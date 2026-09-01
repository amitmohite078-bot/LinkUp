package com.linkup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "group_rules")
public class GroupRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleText;

    public GroupRule() {
    }

    public GroupRule(Long id, Group group, String ruleText) {
        this.id = id;
        this.group = group;
        this.ruleText = ruleText;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getRuleText() {
        return this.ruleText;
    }

    public void setRuleText(String ruleText) {
        this.ruleText = ruleText;
    }

    public static GroupRuleBuilder builder() {
        return new GroupRuleBuilder();
    }

    public static class GroupRuleBuilder {
        private Long id;
        private Group group;
        private String ruleText;

        public GroupRuleBuilder() {}

        public GroupRuleBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GroupRuleBuilder group(Group group) {
            this.group = group;
            return this;
        }

        public GroupRuleBuilder ruleText(String ruleText) {
            this.ruleText = ruleText;
            return this;
        }

        public GroupRule build() {
            return new GroupRule(this.id, this.group, this.ruleText);
        }
    }
}
