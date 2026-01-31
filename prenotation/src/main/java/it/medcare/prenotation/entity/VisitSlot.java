package it.medcare.prenotation.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visit_slot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slotId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @ManyToOne
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @Column(name = "visit_date", columnDefinition = "date")
    @JdbcTypeCode(SqlTypes.DATE)
    private LocalDate visitDate;

    @Column(name = "start_time", columnDefinition = "time")
    @JdbcTypeCode(SqlTypes.TIME)
    private LocalTime startTime;

    @Column(name = "end_time", columnDefinition = "time")
    @JdbcTypeCode(SqlTypes.TIME)
    private LocalTime endTime;
    private Boolean available;
}
