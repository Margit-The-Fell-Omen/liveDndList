package dev.ushki.live_dnd_list.example;


import org.springframework.data.jpa.repository.JpaRepository;

interface EmployeeRepository extends JpaRepository<Employee, Long> {
   
}
