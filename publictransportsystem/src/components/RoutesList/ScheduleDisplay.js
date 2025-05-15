import React from 'react';
import './ScheduleDisplay.css';

const ScheduleDisplay = ({ schedules }) => {
    // Group schedules by hour for better display
    const groupedSchedules = {};

    // Process schedules data
    if (schedules && schedules.length > 0) {
        schedules.forEach(schedule => {
            const time = new Date(schedule.departureTime);
            const hour = time.getHours();
            const minute = time.getMinutes();

            if (!groupedSchedules[hour]) {
                groupedSchedules[hour] = [];
            }

            groupedSchedules[hour].push({
                id: schedule.id,
                time: `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
            });
        });
    }

    // Get all hours from 5 to 22 (typical service hours)
    const hours = Array.from({ length: 18 }, (_, i) => i + 5);

    return (
        <div className="schedule-display">
            {/* Remove day selector - we just show the schedule grid */}
            <div className="schedule-times">
                {hours.map(hour => (
                    <div className="hour-row" key={hour}>
                        {groupedSchedules[hour] && groupedSchedules[hour].map(schedule => (
                            <div className="time-cell" key={schedule.id}>
                                {schedule.time}
                            </div>
                        ))}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ScheduleDisplay;