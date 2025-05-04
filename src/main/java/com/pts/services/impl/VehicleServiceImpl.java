/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.services.impl;

import com.pts.pojo.Vehicles;
import com.pts.repositories.VehicleRepository;
import com.pts.services.VehicleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public List<Vehicles> getAllVehicles() {
        return vehicleRepository.getAllVehicles();
    }

    @Override
    public Vehicles getVehicleById(Integer id) {
        return vehicleRepository.getVehicleById(id);
    }

    @Override
    public Vehicles getVehicleByLicensePlate(String licensePlate) {
        return vehicleRepository.getVehicleByLicensePlate(licensePlate);
    }

    @Override
    public List<Vehicles> getVehiclesByUserId(Integer userId) {
        return vehicleRepository.getVehiclesByUserId(userId);
    }

    @Override
    public boolean addVehicle(Vehicles vehicle) {
        return vehicleRepository.addVehicle(vehicle);
    }

    @Override
    public boolean updateVehicle(Vehicles vehicle) {
        return vehicleRepository.updateVehicle(vehicle);
    }

    @Override
    public boolean deleteVehicle(Integer id) {
        return vehicleRepository.deleteVehicle(id);
    }
}
