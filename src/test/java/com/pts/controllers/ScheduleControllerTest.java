package com.pts.controllers;

import com.pts.pojo.Schedule;
import com.pts.services.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
    }

    @Test
    void getAllSchedules() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.getAllSchedules()).thenReturn(Arrays.asList(schedule));

        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getScheduleById() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.getScheduleById(1L)).thenReturn(Optional.of(schedule));

        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createSchedule() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.createSchedule(any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(post("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"departureTime\":\"2024-01-01T10:00:00\",\"arrivalTime\":\"2024-01-01T11:00:00\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateSchedule() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.updateSchedule(anyLong(), any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(put("/api/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"departureTime\":\"2024-01-01T10:00:00\",\"arrivalTime\":\"2024-01-01T11:00:00\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteSchedule() throws Exception {
        doNothing().when(scheduleService).deleteSchedule(1L);

        mockMvc.perform(delete("/api/schedules/1"))
                .andExpect(status().isOk());
    }
} 