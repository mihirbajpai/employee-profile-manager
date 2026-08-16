package com.example.employeeprofile

import androidx.compose.ui.window.ComposeUIViewController
import com.example.employeeprofile.view.EmployeeApp

/** Entry point for the SwiftUI host in iosApp/ — see ContentView.swift. */
fun MainViewController() = ComposeUIViewController { EmployeeApp() }
