/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.services;

import com.pts.pojo.Vehicles;
import java.util.List;

public interface VehicleService {
    List<Vehicles> getAllVehicles();
    Vehicles getVehicleById(Integer id);
    Vehicles getVehicleByLicensePlate(String licensePlate);
    boolean addVehicle(Vehicles vehicle);
    boolean updateVehicle(Vehicles vehicle);
    boolean deleteVehicle(Integer id);
}
