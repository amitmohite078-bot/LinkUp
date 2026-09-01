package com.linkup.controller;

import com.linkup.config.DataInitializer;
import com.linkup.model.User;
import com.linkup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private UserRepository userRepository;

    public static class DemoUserDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String coverUrl;
        private String bio;
        private String work;
        private String location;
        private String defaultPassword;

        public DemoUserDto() {}

        public DemoUserDto(Long id, String username, String email, String firstName, String lastName,
                           String avatarUrl, String coverUrl, String bio, String work, String location, String defaultPassword) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.avatarUrl = avatarUrl;
            this.coverUrl = coverUrl;
            this.bio = bio;
            this.work = work;
            this.location = location;
            this.defaultPassword = defaultPassword;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getWork() { return work; }
        public void setWork(String work) { this.work = work; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDefaultPassword() { return defaultPassword; }
        public void setDefaultPassword(String defaultPassword) { this.defaultPassword = defaultPassword; }

        public static DemoUserDtoBuilder builder() {
            return new DemoUserDtoBuilder();
        }

        public static class DemoUserDtoBuilder {
            private Long id;
            private String username;
            private String email;
            private String firstName;
            private String lastName;
            private String avatarUrl;
            private String coverUrl;
            private String bio;
            private String work;
            private String location;
            private String defaultPassword;

            public DemoUserDtoBuilder() {}

            public DemoUserDtoBuilder id(Long id) { this.id = id; return this; }
            public DemoUserDtoBuilder username(String username) { this.username = username; return this; }
            public DemoUserDtoBuilder email(String email) { this.email = email; return this; }
            public DemoUserDtoBuilder firstName(String firstName) { this.firstName = firstName; return this; }
            public DemoUserDtoBuilder lastName(String lastName) { this.lastName = lastName; return this; }
            public DemoUserDtoBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
            public DemoUserDtoBuilder coverUrl(String coverUrl) { this.coverUrl = coverUrl; return this; }
            public DemoUserDtoBuilder bio(String bio) { this.bio = bio; return this; }
            public DemoUserDtoBuilder work(String work) { this.work = work; return this; }
            public DemoUserDtoBuilder location(String location) { this.location = location; return this; }
            public DemoUserDtoBuilder defaultPassword(String defaultPassword) { this.defaultPassword = defaultPassword; return this; }

            public DemoUserDto build() {
                return new DemoUserDto(id, username, email, firstName, lastName, avatarUrl, coverUrl, bio, work, location, defaultPassword);
            }
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<DemoUserDto>> getDemoUsers() {
        List<String> demoUsernames = List.of(
                "alex.morgan",
                "sarah.jenkins",
                "david.chen",
                "elena.rostova",
                "marcus.vance",
                "priya.patel"
        );

        List<DemoUserDto> result = new ArrayList<>();
        for (String username : demoUsernames) {
            userRepository.findByUsername(username).ifPresent(user -> {
                result.add(DemoUserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .avatarUrl(user.getAvatarUrl())
                        .coverUrl(user.getCoverUrl())
                        .bio(user.getBio())
                        .work(user.getWork())
                        .location(user.getLocation())
                        .defaultPassword("demo123")
                        .build());
            });
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/seed")
    public ResponseEntity<String> reseedDemoData() {
        try {
            dataInitializer.seedDemoData();
            return ResponseEntity.ok("Demo profiles and content successfully seeded.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to seed demo data: " + e.getMessage());
        }
    }
}
