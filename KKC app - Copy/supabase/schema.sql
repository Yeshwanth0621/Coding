create extension if not exists pgcrypto;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'club-media',
    'club-media',
    true,
    3145728,
    array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update
set public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

create table if not exists public.club_profiles (
    user_id uuid primary key references auth.users (id) on delete cascade,
    club_id text not null unique,
    club_name text not null,
    club_logo_url text not null default '',
    club_banner_color_hex text not null default '#0EA5E9',
    contact_name text not null default '',
    contact_email text not null default '',
    description text not null default '',
    updated_at_millis bigint not null default 0
);

create table if not exists public.events (
    id uuid primary key default gen_random_uuid(),
    club_owner_id uuid,
    club_id text not null default '',
    club_name text not null default '',
    club_logo_url text not null default '',
    club_banner_color_hex text not null default '#0EA5E9',
    title text not null,
    summary text not null,
    description text not null,
    location text not null default '',
    image_url text not null default '',
    registration_link text not null default '',
    notify_one_hour_before boolean not null default true,
    start_at_millis bigint not null,
    end_at_millis bigint not null,
    featured boolean not null default false,
    created_at_millis bigint not null,
    updated_at_millis bigint not null,
    author_email text not null default ''
);

alter table public.events add column if not exists club_owner_id uuid;
alter table public.events add column if not exists club_id text not null default '';
alter table public.events add column if not exists club_name text not null default '';
alter table public.events add column if not exists club_logo_url text not null default '';
alter table public.events add column if not exists club_banner_color_hex text not null default '#0EA5E9';
alter table public.events add column if not exists image_url text not null default '';
alter table public.events add column if not exists registration_link text not null default '';
alter table public.events add column if not exists notify_one_hour_before boolean not null default true;
alter table public.club_profiles add column if not exists club_banner_color_hex text not null default '#0EA5E9';

alter table public.club_profiles enable row level security;
alter table public.events enable row level security;

drop policy if exists "Public read events" on public.events;
create policy "Public read events"
on public.events
for select
using (true);

drop policy if exists "Club owners insert events" on public.events;
create policy "Club owners insert events"
on public.events
for insert
to authenticated
with check (
    club_owner_id = auth.uid()
    and exists (
        select 1
        from public.club_profiles
        where club_profiles.user_id = auth.uid()
    )
);

drop policy if exists "Club owners update events" on public.events;
create policy "Club owners update events"
on public.events
for update
to authenticated
using (club_owner_id = auth.uid())
with check (club_owner_id = auth.uid());

drop policy if exists "Club owners delete events" on public.events;
create policy "Club owners delete events"
on public.events
for delete
to authenticated
using (club_owner_id = auth.uid());

drop policy if exists "Public cleanup expired events" on public.events;
create policy "Public cleanup expired events"
on public.events
for delete
to anon, authenticated
using (
    end_at_millis < (extract(epoch from now()) * 1000)::bigint
);

drop policy if exists "Owners insert own club profile" on public.club_profiles;
create policy "Owners insert own club profile"
on public.club_profiles
for insert
to authenticated
with check (user_id = auth.uid());

drop policy if exists "Owners read own club profile" on public.club_profiles;
drop policy if exists "Public read club profiles" on public.club_profiles;
create policy "Public read club profiles"
on public.club_profiles
for select
using (true);

drop policy if exists "Owners update own club profile" on public.club_profiles;
create policy "Owners update own club profile"
on public.club_profiles
for update
to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());

drop policy if exists "Public read club media objects" on storage.objects;
create policy "Public read club media objects"
on storage.objects
for select
using (bucket_id = 'club-media');

drop policy if exists "Club owners upload media" on storage.objects;
create policy "Club owners upload media"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'club-media'
    and (storage.foldername(name))[1] = (select auth.uid()::text)
);

drop policy if exists "Club owners update media" on storage.objects;
create policy "Club owners update media"
on storage.objects
for update
to authenticated
using (
    bucket_id = 'club-media'
    and (storage.foldername(name))[1] = (select auth.uid()::text)
)
with check (
    bucket_id = 'club-media'
    and (storage.foldername(name))[1] = (select auth.uid()::text)
);

drop policy if exists "Club owners delete media" on storage.objects;
create policy "Club owners delete media"
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'club-media'
    and (storage.foldername(name))[1] = (select auth.uid()::text)
);

drop policy if exists "Public cleanup expired event posters" on storage.objects;
create policy "Public cleanup expired event posters"
on storage.objects
for delete
to anon, authenticated
using (
    bucket_id = 'club-media'
    and exists (
        select 1
        from public.events
        where events.end_at_millis < (extract(epoch from now()) * 1000)::bigint
          and events.image_url like '%' || storage.objects.name
    )
);

do $$
begin
    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename = 'events'
    ) then
        alter publication supabase_realtime add table public.events;
    end if;
end $$;
