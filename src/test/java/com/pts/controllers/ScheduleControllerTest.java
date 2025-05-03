package com.pts.controllers;

import com.pts.pojo.Schedule;
import com.pts.services.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllSchedules() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.getAllSchedules()).thenReturn(Arrays.asList(schedule));

        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getScheduleById() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.getScheduleById(1L)).thenReturn(Optional.of(schedule));

        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getScheduleById_NotFound() throws Exception {
        when(scheduleService.getScheduleById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSchedule() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setDepartureTime(LocalDateTime.now());
        schedule.setArrivalTime(LocalDateTime.now().plusHours(1));
        schedule.setStatus("ACTIVE");

        when(scheduleService.createSchedule(any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(post("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(schedule)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateSchedule() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setDepartureTime(LocalDateTime.now());
        schedule.setArrivalTime(LocalDateTime.now().plusHours(1));
        schedule.setStatus("ACTIVE");

        when(scheduleService.updateSchedule(anyLong(), any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(put("/api/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(schedule)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteSchedule() throws Exception {
        doNothing().when(scheduleService).deleteSchedule(1L);

        mockMvc.perform(delete("/api/schedules/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getSchedulesByVehicle() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.getSchedulesByVehicle(1L)).thenReturn(Arrays.asList(schedule));

        mockMvc.perform(get("/api/schedules/vehicle/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getSchedulesByRoute() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        when(scheduleService.getSchedulesByRoute(1L)).thenReturn(Arrays.asList(schedule));

        mockMvc.perform(get("/api/schedules/route/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }
} 