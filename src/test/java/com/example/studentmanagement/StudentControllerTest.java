package com.example.studentmanagement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentControllerTest {

    @Test
    void addStudentTest() {

        StudentController controller = new StudentController();

        Student student =
                new Student(1, "Lakshmi", "CSE");

        Student result =
                controller.addStudent(student);

        assertEquals(1, result.getId());
        assertEquals("Lakshmi", result.getName());
        assertEquals("CSE", result.getDepartment()) ;
    }
}