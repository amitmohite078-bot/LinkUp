package com.linkup.config;

import com.linkup.model.*;
import com.linkup.repository.*;
import com.linkup.service.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventAttendeeRepository eventAttendeeRepository;

    @Override
    public void run(String... args) {
        seedDemoData();
    }

    @Transactional
    public void seedDemoData() {
        // Only seed if user count is low or demo user doesn't exist
        if (userRepository.existsByUsername("alex.morgan")) {
            return;
        }

        String defaultHashedPassword = PasswordUtil.hashPassword("demo123");

        // 1. Create Demo Profiles
        User alex = User.builder()
                .username("alex.morgan")
                .email("alex.morgan@example.com")
                .passwordHash(defaultHashedPassword)
                .firstName("Alex")
                .lastName("Morgan")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80")
                .coverUrl("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&auto=format&fit=crop&q=80")
                .bio("Lead Full-Stack Architect & Open-Source enthusiast. Coffee lover, hiker, and building real-time distributed systems.")
                .work("Principal Architect @ CloudScale Tech")
                .education("Stanford University (M.S. Computer Science)")
                .location("San Francisco, California")
                .relationshipStatus("In a relationship")
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        User sarah = User.builder()
                .username("sarah.jenkins")
                .email("sarah.jenkins@example.com")
                .passwordHash(defaultHashedPassword)
                .firstName("Sarah")
                .lastName("Jenkins")
                .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80")
                .coverUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80")
                .bio("National Geographic contributor & Landscape Photographer 📸 Wandering through mountains and capturing natural light.")
                .work("Freelance Visual Journalist @ Jenkins Studios")
                .education("Rhode Island School of Design")
                .location("Seattle, Washington")
                .relationshipStatus("Single")
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        User david = User.builder()
                .username("david.chen")
                .email("david.chen@example.com")
                .passwordHash(defaultHashedPassword)
                .firstName("David")
                .lastName("Chen")
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80")
                .coverUrl("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200&auto=format&fit=crop&q=80")
                .bio("Sound Designer & Electronic Music Producer 🎧 Synthesizers, vinyl records, and late night studio sessions.")
                .work("Senior Audio Designer @ Waveform Labs")
                .education("Berklee College of Music")
                .location("Austin, Texas")
                .relationshipStatus("Engaged")
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        User elena = User.builder()
                .username("elena.rostova")
                .email("elena.rostova@example.com")
                .passwordHash(defaultHashedPassword)
                .firstName("Elena")
                .lastName("Rostova")
                .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80")
                .coverUrl("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80")
                .bio("Design Director & Creative Technologist ✨ Crafting playful digital interfaces, dark-mode designs, and spatial 3D experiences.")
                .work("Lead Product Designer @ Studio Neo")
                .education("Parsons School of Design")
                .location("New York, New York")
                .relationshipStatus("Single")
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        User marcus = User.builder()
                .username("marcus.vance")
                .email("marcus.vance@example.com")
                .passwordHash(defaultHashedPassword)
                .firstName("Marcus")
                .lastName("Vance")
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80")
                .coverUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200&auto=format&fit=crop&q=80")
                .bio("Ultra-marathoner, Fitness Coach & Mountain Guide 🏔️ Helping athletes push endurance and mental toughness.")
                .work("Founder & Head Trainer @ Peak Performance Club")
                .education("University of Colorado Boulder")
                .location("Denver, Colorado")
                .relationshipStatus("Married")
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        User priya = User.builder()
                .username("priya.patel")
                .email("priya.patel@example.com")
                .passwordHash(defaultHashedPassword)
                .firstName("Priya")
                .lastName("Patel")
                .avatarUrl("https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80")
                .coverUrl("https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&auto=format&fit=crop&q=80")
                .bio("AI Research Scientist 🤖 Keynote Speaker, robotics geek, and passionate about ethical intelligence and neural architectures.")
                .work("Research Scientist @ DeepMind Research")
                .education("MIT (Ph.D. in Artificial Intelligence)")
                .location("Boston, Massachusetts")
                .relationshipStatus("Single")
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        List<User> savedUsers = userRepository.saveAll(Arrays.asList(alex, sarah, david, elena, marcus, priya));
        alex = savedUsers.get(0);
        sarah = savedUsers.get(1);
        david = savedUsers.get(2);
        elena = savedUsers.get(3);
        marcus = savedUsers.get(4);
        priya = savedUsers.get(5);

        // 2. Friendships & Connections
        List<Friendship> friendships = new ArrayList<>();
        // Accepted friendships
        friendships.add(Friendship.builder().requester(alex).addressee(sarah).status("ACCEPTED").build());
        friendships.add(Friendship.builder().requester(alex).addressee(david).status("ACCEPTED").build());
        friendships.add(Friendship.builder().requester(alex).addressee(elena).status("ACCEPTED").build());
        friendships.add(Friendship.builder().requester(sarah).addressee(elena).status("ACCEPTED").build());
        friendships.add(Friendship.builder().requester(david).addressee(marcus).status("ACCEPTED").build());
        // Pending Friend Requests (e.g. Priya -> Alex so Alex sees incoming request!)
        friendships.add(Friendship.builder().requester(priya).addressee(alex).status("PENDING").build());
        friendships.add(Friendship.builder().requester(marcus).addressee(alex).status("PENDING").build());

        friendshipRepository.saveAll(friendships);

        // 3. Demo Posts
        Post p1 = postRepository.save(Post.builder()
                .user(sarah)
                .content("Golden hour over Mount Rainier yesterday. The alpine glow and morning mist were absolutely breathtaking! 🌄📸")
                .feelingActivity("feeling peaceful 🌿")
                .location("Mount Rainier National Park")
                .privacy("PUBLIC")
                .type("PHOTO")
                .build());

        postMediaRepository.save(PostMedia.builder()
                .post(p1)
                .mediaUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1000&auto=format&fit=crop&q=80")
                .mediaType("IMAGE")
                .build());

        Post p2 = postRepository.save(Post.builder()
                .user(alex)
                .content("Excited to announce that our new open-source distributed event processing engine is live on GitHub! 🚀 Scaled to 1M msgs/sec with sub-millisecond latency. Feedback and pull requests welcome!")
                .feelingActivity("feeling excited 💡")
                .location("San Francisco, CA")
                .privacy("PUBLIC")
                .type("TEXT")
                .build());

        Post p3 = postRepository.save(Post.builder()
                .user(elena)
                .content("Experimenting with dynamic glassmorphism and subtle micro-animations for our new dark-mode design system. What do you think of this interface balance? ✨🎨")
                .feelingActivity("feeling creative 🎨")
                .location("Studio Neo, NYC")
                .privacy("PUBLIC")
                .type("PHOTO")
                .build());

        postMediaRepository.save(PostMedia.builder()
                .post(p3)
                .mediaUrl("https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=1000&auto=format&fit=crop&q=80")
                .mediaType("IMAGE")
                .build());

        Post p4 = postRepository.save(Post.builder()
                .user(david)
                .content("Wrapped up recording analog synth layers for the new ambient album! 🎹 Master vinyl cuts head to production next week.")
                .feelingActivity("feeling inspired 🎵")
                .location("Waveform Studio, Austin")
                .privacy("PUBLIC")
                .type("PHOTO")
                .build());

        postMediaRepository.save(PostMedia.builder()
                .post(p4)
                .mediaUrl("https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=1000&auto=format&fit=crop&q=80")
                .mediaType("IMAGE")
                .build());

        Post p5 = postRepository.save(Post.builder()
                .user(priya)
                .content("Honored to deliver the keynote on Autonomous Neural Agents at the Boston AI Symposium today! The future of collaborative AI workflows is here. 🤖💡")
                .feelingActivity("feeling proud 🌟")
                .location("Boston Convention Center")
                .privacy("PUBLIC")
                .type("PHOTO")
                .build());

        postMediaRepository.save(PostMedia.builder()
                .post(p5)
                .mediaUrl("https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=1000&auto=format&fit=crop&q=80")
                .mediaType("IMAGE")
                .build());

        Post p6 = postRepository.save(Post.builder()
                .user(marcus)
                .content("Early morning 20k trail run through the Rockies! Remember: consistency over intensity every single day. 💪🏔️")
                .feelingActivity("feeling energized ⚡")
                .location("Boulder Foothills, CO")
                .privacy("PUBLIC")
                .type("TEXT")
                .build());

        // 4. Reactions & Comments on posts
        postReactionRepository.save(PostReaction.builder().post(p1).user(alex).reactionType("LOVE").build());
        postReactionRepository.save(PostReaction.builder().post(p1).user(elena).reactionType("WOW").build());
        postReactionRepository.save(PostReaction.builder().post(p1).user(david).reactionType("LIKE").build());
        postReactionRepository.save(PostReaction.builder().post(p2).user(priya).reactionType("LIKE").build());
        postReactionRepository.save(PostReaction.builder().post(p2).user(david).reactionType("LOVE").build());
        postReactionRepository.save(PostReaction.builder().post(p3).user(alex).reactionType("LOVE").build());
        postReactionRepository.save(PostReaction.builder().post(p3).user(sarah).reactionType("LIKE").build());

        commentRepository.save(Comment.builder()
                .post(p1)
                .user(alex)
                .content("Incredible composition Sarah! What lens did you use for this shot?")
                .build());

        commentRepository.save(Comment.builder()
                .post(p1)
                .user(sarah)
                .content("Thanks Alex! Used a 24-70mm f/2.8 with a circular polarizer at sunrise.")
                .build());

        commentRepository.save(Comment.builder()
                .post(p3)
                .user(alex)
                .content("The glow effect and color balance look super crisp! Love the typography hierarchy.")
                .build());

        // 5. Demo Stories (24hr ephemeral status)
        storyRepository.save(Story.builder()
                .user(sarah)
                .textContent("Sunrise coffee & misty valley views ☕🌲")
                .emoji("🌲")
                .musicTitle("Bon Iver - Holocene")
                .mediaUrl("https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80")
                .privacy("PUBLIC")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        storyRepository.save(Story.builder()
                .user(elena)
                .textContent("Late night wireframing & prototyping ✨")
                .emoji("✨")
                .musicTitle("Tycho - Awake")
                .mediaUrl("https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=800&auto=format&fit=crop&q=80")
                .privacy("PUBLIC")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        storyRepository.save(Story.builder()
                .user(marcus)
                .textContent("Summit conquered! 3,400 meters 🏔️")
                .emoji("🏔️")
                .musicTitle("M83 - Outro")
                .mediaUrl("https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=800&auto=format&fit=crop&q=80")
                .privacy("PUBLIC")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        // 6. Demo Groups
        Group g1 = groupRepository.save(Group.builder()
                .name("Tech & AI Innovators")
                .description("A community for engineers, researchers, and creators exploring distributed systems, real-time web, and artificial intelligence.")
                .creator(alex)
                .privacy("PUBLIC")
                .build());

        groupMemberRepository.save(GroupMember.builder().group(g1).user(alex).role("ADMIN").status("ACTIVE").build());
        groupMemberRepository.save(GroupMember.builder().group(g1).user(priya).role("MODERATOR").status("ACTIVE").build());
        groupMemberRepository.save(GroupMember.builder().group(g1).user(elena).role("MEMBER").status("ACTIVE").build());

        Group g2 = groupRepository.save(Group.builder()
                .name("Visual Storytellers & Photographers")
                .description("Sharing landscapes, street photography, lighting gear tips, and creative post-processing workflows.")
                .creator(sarah)
                .privacy("PUBLIC")
                .build());

        groupMemberRepository.save(GroupMember.builder().group(g2).user(sarah).role("ADMIN").status("ACTIVE").build());
        groupMemberRepository.save(GroupMember.builder().group(g2).user(alex).role("MEMBER").status("ACTIVE").build());
        groupMemberRepository.save(GroupMember.builder().group(g2).user(david).role("MEMBER").status("ACTIVE").build());

        // 7. Demo Events
        Event e1 = eventRepository.save(Event.builder()
                .name("Global Tech & Cloud Summit 2026")
                .description("Annual technology keynote featuring breakthroughs in distributed computing, edge AI, and high-performance web applications.")
                .coverUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1000&auto=format&fit=crop&q=80")
                .dateTime(LocalDateTime.now().plusDays(4).withHour(18).withMinute(0))
                .location("Moscone Center, SF & Live Stream")
                .type("PHYSICAL")
                .privacy("PUBLIC")
                .creator(alex)
                .build());

        eventAttendeeRepository.save(EventAttendee.builder().event(e1).user(alex).status("GOING").build());
        eventAttendeeRepository.save(EventAttendee.builder().event(e1).user(priya).status("GOING").build());
        eventAttendeeRepository.save(EventAttendee.builder().event(e1).user(elena).status("INTERESTED").build());

        Event e2 = eventRepository.save(Event.builder()
                .name("Golden Hour Photography Walk")
                .description("Join fellow landscape & street photographers for a sunset photowalk through the coastline.")
                .coverUrl("https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=1000&auto=format&fit=crop&q=80")
                .dateTime(LocalDateTime.now().plusDays(8).withHour(17).withMinute(30))
                .location("Olympic Sculpture Park, Seattle")
                .type("PHYSICAL")
                .privacy("PUBLIC")
                .creator(sarah)
                .build());

        eventAttendeeRepository.save(EventAttendee.builder().event(e2).user(sarah).status("GOING").build());
        eventAttendeeRepository.save(EventAttendee.builder().event(e2).user(david).status("GOING").build());
    }
}
