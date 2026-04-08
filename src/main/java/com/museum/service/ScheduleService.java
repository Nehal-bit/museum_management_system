package com.museum.service;

import com.museum.dto.ScheduleRequestDTO;
import com.museum.model.*;
import com.museum.repository.ExhibitRepository;
import com.museum.repository.ScheduleRepository;
import com.museum.repository.VisitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ExhibitRepository exhibitRepository;
    private final VisitorRepository visitorRepository;
    private final NotificationManager notificationManager;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           ExhibitRepository exhibitRepository,
                           VisitorRepository visitorRepository,
                           NotificationManager notificationManager) {
        this.scheduleRepository = scheduleRepository;
        this.exhibitRepository = exhibitRepository;
        this.visitorRepository = visitorRepository;
        this.notificationManager = notificationManager;
    }

    @Transactional
    public Schedule requestSchedule(ScheduleRequestDTO dto) {
        Exhibit exhibit = exhibitRepository.findById(dto.getExhibitId())
                .orElseThrow(() -> new RuntimeException("Exhibit not found: " + dto.getExhibitId()));
        Visitor visitor = visitorRepository.findById(dto.getVisitorId())
                .orElseThrow(() -> new RuntimeException("Visitor not found: " + dto.getVisitorId()));

        Schedule schedule = new Schedule();
        schedule.setExhibit(exhibit);
        schedule.setRequestedBy(visitor);
        schedule.setVisitDate(dto.getVisitDate());
        schedule.setStartTime(dto.getResolvedStartTime());
        schedule.setEndTime(dto.getResolvedEndTime());
        schedule.setMaxVisitors(dto.getMaxVisitors() > 0 ? dto.getMaxVisitors() : 30);
        schedule.setStatus(ScheduleStatus.PENDING);

        Schedule saved = scheduleRepository.save(schedule);

        notificationManager.notifyUser(visitor,
                "Your visit request for exhibit '" + exhibit.getName() +
                "' on " + dto.getVisitDate() + " at " + schedule.getStartTime() +
                " has been submitted and is pending approval.");

        return saved;
    }

    @Transactional
    public Schedule approveSchedule(Long scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);
        if (schedule.getStatus() != ScheduleStatus.PENDING)
            throw new RuntimeException("Schedule is not PENDING.");
        schedule.setStatus(ScheduleStatus.APPROVED);
        Schedule approved = scheduleRepository.save(schedule);
        if (schedule.getRequestedBy() != null) {
            notificationManager.notifyUser(schedule.getRequestedBy(),
                    "✅ Your visit to '" + schedule.getExhibit().getName() +
                    "' on " + schedule.getVisitDate() + " at " + schedule.getStartTime() +
                    " has been APPROVED!");
        }
        return approved;
    }

    @Transactional
    public Schedule rejectSchedule(Long scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);
        if (schedule.getStatus() != ScheduleStatus.PENDING)
            throw new RuntimeException("Schedule is not PENDING.");
        schedule.setStatus(ScheduleStatus.REJECTED);
        Schedule rejected = scheduleRepository.save(schedule);
        if (schedule.getRequestedBy() != null) {
            notificationManager.notifyUser(schedule.getRequestedBy(),
                    "❌ Your visit request for '" + schedule.getExhibit().getName() +
                    "' on " + schedule.getVisitDate() + " has been REJECTED.");
        }
        return rejected;
    }

    @Transactional(readOnly = true)
    public List<Schedule> checkAvailability(Long exhibitId, LocalDate date) {
        return scheduleRepository.findAvailableSlots(exhibitId, date)
                .stream().filter(Schedule::hasAvailableSlots).toList();
    }

    @Transactional(readOnly = true) public List<Schedule> getAllSchedules() { return scheduleRepository.findAll(); }
    @Transactional(readOnly = true) public List<Schedule> getPendingSchedules() { return scheduleRepository.findByStatus(ScheduleStatus.PENDING); }
    @Transactional(readOnly = true) public List<Schedule> getSchedulesByExhibit(Long exhibitId) { return scheduleRepository.findByExhibit_ExhibitId(exhibitId); }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByVisitor(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new RuntimeException("Visitor not found: " + visitorId));
        return scheduleRepository.findByRequestedBy(visitor);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + id));
    }
}
