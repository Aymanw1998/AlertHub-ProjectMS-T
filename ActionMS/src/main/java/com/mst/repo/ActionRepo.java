package com.mst.repo;

import com.mst.model.Action;
import com.mst.model.RunOnDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepo extends JpaRepository<Action, Long> {

    @Query("SELECT a FROM Action a WHERE a.is_enabled = true AND a.is_deleted = false AND (a.run_on_day = :currentDay OR a.run_on_day = 'All')")
    List<Action> findActiveActionsForToday(RunOnDay currentDay);
}
