package com.techacademy.repository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.ui.Model;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    public List<Report> findByEmployeeAndReportDate(Employee employee, LocalDate reportdate);

}