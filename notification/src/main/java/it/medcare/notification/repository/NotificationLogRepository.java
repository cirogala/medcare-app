package it.medcare.notification.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.medcare.notification.entity.NotificationLog;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {}
