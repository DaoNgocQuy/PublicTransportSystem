import React from 'react';
import './ScheduleDisplay.css';

const ScheduleDisplay = ({ schedules }) => {
    console.log('📊 ScheduleDisplay received schedules:', schedules);

    if (!schedules || schedules.length === 0) {
        return (
            <div className="schedule-display">
                <h3>Biểu đồ giờ</h3>
                <p className="no-schedule">Chưa có lịch trình cho tuyến này</p>
            </div>
        );
    }

    // Function to parse time from various formats
    const parseTime = (timeData) => {
        if (!timeData) return null;

        try {
            let timeObj;

            // If it's already a Date object
            if (timeData instanceof Date) {
                timeObj = timeData;
            }
            // If it's a string that looks like a time (HH:MM or HH:MM:SS)
            else if (typeof timeData === 'string' && timeData.match(/^\d{1,2}:\d{2}/)) {
                const [hours, minutes] = timeData.split(':').map(Number);
                timeObj = new Date();
                timeObj.setHours(hours, minutes, 0, 0);
            }
            // If it's a timestamp or ISO string
            else if (typeof timeData === 'string' || typeof timeData === 'number') {
                timeObj = new Date(timeData);
            }
            // If it's an object with time properties
            else if (typeof timeData === 'object') {
                if (timeData.time) {
                    return parseTime(timeData.time);
                } else if (timeData.hours !== undefined && timeData.minutes !== undefined) {
                    timeObj = new Date();
                    timeObj.setHours(timeData.hours, timeData.minutes, 0, 0);
                } else {
                    timeObj = new Date(timeData);
                }
            }
            else {
                console.warn('Unknown time format:', timeData);
                return null;
            }

            // Check if the date is valid
            if (isNaN(timeObj.getTime())) {
                console.warn('Invalid time data:', timeData);
                return null;
            }

            return timeObj;
        } catch (error) {
            console.error('Error parsing time:', error, timeData);
            return null;
        }
    };

    // Group schedules by hour for better display
    const groupedSchedules = {};
    const validSchedules = [];

    // Process schedules data
    schedules.forEach((schedule, index) => {
        console.log(`Processing schedule ${index}:`, schedule);

        const timeObj = parseTime(schedule.departureTime);

        if (timeObj) {
            const hour = timeObj.getHours();
            const minute = timeObj.getMinutes();

            if (!groupedSchedules[hour]) {
                groupedSchedules[hour] = [];
            }

            const processedSchedule = {
                id: schedule.id || index,
                time: `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`,
                vehicle: schedule.vehicleId,
                originalData: schedule
            };

            groupedSchedules[hour].push(processedSchedule);
            validSchedules.push(processedSchedule);

            console.log(`✅ Processed schedule: ${processedSchedule.time}`);
        } else {
            console.warn(`⚠️ Could not process schedule:`, schedule);
        }
    });

    // Get only hours that have schedules
    const availableHours = Object.keys(groupedSchedules)
        .map(hour => parseInt(hour))
        .sort((a, b) => a - b);

    console.log('📊 Available hours:', availableHours);
    console.log('📊 Valid schedules:', validSchedules.length, 'of', schedules.length);
    console.log('📊 Grouped schedules:', groupedSchedules);

    return (
        <div className="schedule-display">
            <h3>Biểu đồ giờ</h3>
            <div className="schedule-summary">
                <p>Tổng số chuyến: <strong>{schedules.length}</strong></p>
                <p>Lịch trình hợp lệ: <strong>{validSchedules.length}</strong></p>
                <p>Khung giờ hoạt động: <strong>
                    {availableHours.length > 0
                        ? `${availableHours[0]}:00 - ${availableHours[availableHours.length - 1]}:59`
                        : 'Không có dữ liệu'
                    }
                </strong></p>
            </div>

            {availableHours.length > 0 ? (
                <div className="schedule-grid">
                    {availableHours.map(hour => (
                        <div className="hour-section" key={hour}>
                            <h4 className="hour-header">
                                {hour.toString().padStart(2, '0')}:00
                                <span className="schedule-count">
                                    ({groupedSchedules[hour].length} chuyến)
                                </span>
                            </h4>
                            <div className="schedules-in-hour">
                                {groupedSchedules[hour].map((schedule, index) => (
                                    <div className="schedule-item" key={`${hour}-${index}`}>
                                        <span className="departure-time">{schedule.time}</span>
                                        {schedule.vehicle && (
                                            <span className="vehicle-info">
                                                Xe: {schedule.vehicle.vehicleNumber || schedule.vehicle.id || schedule.vehicle}
                                            </span>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="no-schedule">
                    <p>Không có lịch trình hợp lệ</p>
                    {schedules.length > 0 && (
                        <details style={{ marginTop: '10px', fontSize: '12px', color: '#666' }}>
                            <summary>Debug: Dữ liệu thô</summary>
                            <pre style={{ textAlign: 'left', fontSize: '10px', overflow: 'auto' }}>
                                {JSON.stringify(schedules.slice(0, 3), null, 2)}
                            </pre>
                        </details>
                    )}
                </div>
            )}
        </div>
    );
};

export default ScheduleDisplay;