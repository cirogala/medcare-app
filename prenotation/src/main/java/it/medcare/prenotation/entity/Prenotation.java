package it.medcare.prenotation.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.medcare.prenotation.enums.PrenotationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prenotation", uniqueConstraints = @UniqueConstraint( columnNames = {"doctor_id", "date", "slot_time"}))
public class Prenotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prenotationId;

    private Long userId;

    private Long doctorId;

    private Long visitTypeId;

    @Column(name = "slot_id")
    private Long slotId;

    @Column(columnDefinition = "date")
    @JdbcTypeCode(SqlTypes.DATE)
    private LocalDate date;

    @Column(columnDefinition = "time")
    @JdbcTypeCode(SqlTypes.TIME)
    private LocalTime slotTime;

    @Enumerated(EnumType.STRING)
    private PrenotationStatus status;

    private LocalDateTime createdAt;
    
    private Boolean flDeleted;
    
    private LocalDateTime deletedAt;

}

