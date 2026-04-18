import { useState } from 'react';
import './Page.css';

interface TeamMember {
    id: number;
    name: string;
    role: string;
    image?: string;
    bio: string;
    department: string;
    email?: string;
}

const Members = () => {
    const [selectedMember, setSelectedMember] = useState<TeamMember | null>(null);

    const coreTeam: TeamMember[] = [
        {
            id: 1,
            name: 'Priya Sharma',
            role: 'President',
            department: 'Computer Science',
            bio: 'Visionary leader with a passion for innovation and community building.',
            email: 'priya@kkc.com'
        },
        {
            id: 2,
            name: 'Arjun Kumar',
            role: 'Vice President',
            department: 'Electronics & Communication',
            bio: 'Strategic thinker focused on expanding the club\'s reach and impact.',
            email: 'arjun@kkc.com'
        },
        {
            id: 3,
            name: 'Sneha Patel',
            role: 'Secretary',
            department: 'Information Technology',
            bio: 'Organized and detail-oriented, ensures smooth operations and communication.',
            email: 'sneha@kkc.com'
        },
        {
            id: 4,
            name: 'Rahul Verma',
            role: 'Treasurer',
            department: 'Mechanical Engineering',
            bio: 'Financial expert managing resources for impactful events and initiatives.',
            email: 'rahul@kkc.com'
        },
    ];

    const techLeads: TeamMember[] = [
        {
            id: 5,
            name: 'Isha Singh',
            role: 'Technical Lead - Web Development',
            department: 'Computer Science',
            bio: 'Expert in modern web technologies and full-stack development.',
            email: 'isha@kkc.com'
        },
        {
            id: 6,
            name: 'Akshay Desai',
            role: 'Technical Lead - Mobile Development',
            department: 'Information Technology',
            bio: 'Specialized in Android and iOS app development and deployment.',
            email: 'akshay@kkc.com'
        },
        {
            id: 7,
            name: 'Ananya Reddy',
            role: 'Events Coordinator',
            department: 'Computer Science',
            bio: 'Creative and energetic, orchestrates memorable events and workshops.',
            email: 'ananya@kkc.com'
        },
        {
            id: 8,
            name: 'Rohan Gupta',
            role: 'Sponsorship Manager',
            department: 'Business Administration',
            bio: 'Strong networking skills building valuable partnerships and sponsorships.',
            email: 'rohan@kkc.com'
        },
    ];

    const advisors: TeamMember[] = [
        {
            id: 9,
            name: 'Dr. Rajesh Kumar',
            role: 'Faculty Advisor',
            department: 'Computer Science Department',
            bio: 'Guiding the club with academic excellence and industry insights.',
            email: 'rajesh@university.edu'
        },
    ];

    return (
        <div className="page-container section">
            <div className="container">
                <h1 className="section-title fade-in-up">Meet Our <span className="gradient-text">Team</span></h1>
                <p className="section-subtitle fade-in-up">The dedicated individuals making a difference through innovation and collaboration.</p>

                {/* Advisors Section */}
                <div className="team-section">
                    <h2 className="team-section-title">Faculty Advisor</h2>
                    <div className="members-grid">
                        {advisors.map((member, index) => (
                            <div key={member.id} className="glass-card member-card fade-in-up" style={{ animationDelay: `${index * 0.1}s` }} onClick={() => setSelectedMember(member)}>
                                <div className="member-image-placeholder">
                                    <div className="member-avatar">{member.name[0]}</div>
                                </div>
                                <h3>{member.name}</h3>
                                <p className="member-role">{member.role}</p>
                                <p className="member-department">{member.department}</p>
                                <span className="member-badge advisor">Advisor</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Core Team Section */}
                <div className="team-section">
                    <h2 className="team-section-title">Core Team</h2>
                    <div className="members-grid">
                        {coreTeam.map((member, index) => (
                            <div key={member.id} className="glass-card member-card fade-in-up" style={{ animationDelay: `${index * 0.1}s` }} onClick={() => setSelectedMember(member)}>
                                <div className="member-image-placeholder">
                                    <div className="member-avatar">{member.name[0]}</div>
                                </div>
                                <h3>{member.name}</h3>
                                <p className="member-role">{member.role}</p>
                                <p className="member-department">{member.department}</p>
                                <span className="member-badge core">Core</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Tech Leads Section */}
                <div className="team-section">
                    <h2 className="team-section-title">Technical Leads & Coordinators</h2>
                    <div className="members-grid">
                        {techLeads.map((member, index) => (
                            <div key={member.id} className="glass-card member-card fade-in-up" style={{ animationDelay: `${index * 0.1}s` }} onClick={() => setSelectedMember(member)}>
                                <div className="member-image-placeholder">
                                    <div className="member-avatar">{member.name[0]}</div>
                                </div>
                                <h3>{member.name}</h3>
                                <p className="member-role">{member.role}</p>
                                <p className="member-department">{member.department}</p>
                                <span className="member-badge tech">Team</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Member Details Modal */}
                {selectedMember && (
                    <div className="member-modal-overlay" onClick={() => setSelectedMember(null)}>
                        <div className="member-modal glass-card" onClick={(e) => e.stopPropagation()}>
                            <button className="modal-close" onClick={() => setSelectedMember(null)}>✕</button>
                            <div className="modal-content">
                                <div className="modal-avatar">
                                    <div className="member-avatar large">{selectedMember.name[0]}</div>
                                </div>
                                <h2>{selectedMember.name}</h2>
                                <p className="modal-role">{selectedMember.role}</p>
                                <p className="modal-department">{selectedMember.department}</p>
                                <p className="modal-bio">{selectedMember.bio}</p>
                                {selectedMember.email && (
                                    <div className="modal-contact">
                                        <a href={`mailto:${selectedMember.email}`} className="btn btn-primary">
                                            Get in Touch
                                        </a>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Members;
