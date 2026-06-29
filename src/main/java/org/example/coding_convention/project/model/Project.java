package org.example.coding_convention.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.coding_convention.chat.model.Chats;
import org.example.coding_convention.file.model.Files;
import org.example.coding_convention.likes.model.Like;
import org.example.coding_convention.project_member.model.ProjectMember;
import org.example.coding_convention.user.model.User;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false, length = 50)
    private String projectName;

    private String url;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    public enum Language {
        JAVASCRIPT, JAVA, PYTHON, C
    }

    public static Language projectLanguage(String language) {
        return Language.valueOf(language.toUpperCase());
    }

    // ("creator_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    private List<Files> fileList;

    @BatchSize(size = 20)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    private List<ProjectMember> projectMemberList;

    @BatchSize(size = 100)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    private List<Chats> chatsList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    private List<Like> likeList;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    // 편의 메소드
    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
