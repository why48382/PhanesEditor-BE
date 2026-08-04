package org.example.coding_convention.project_member.repository;

import org.example.coding_convention.project_member.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    List<ProjectMember> findByUser_Idx(Integer userId);

    List<ProjectMember> findByProject_Idx(Integer projectId);

    @Query("SELECT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.project p " +
            "JOIN FETCH p.user " +
            "WHERE pm.user.idx = :userId")
    List<ProjectMember> findByProjectList(@Param("userId") Integer userId);

    boolean existsByProject_IdxAndUser_Idx(Integer projectId, Integer userId);
}
