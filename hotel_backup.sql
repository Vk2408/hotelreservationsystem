-- MySQL dump 10.13  Distrib 9.0.1, for Win64 (x86_64)
--
-- Host: localhost    Database: hotel
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cancellations`
--

DROP TABLE IF EXISTS `cancellations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cancellations` (
  `cancellation_id` int NOT NULL AUTO_INCREMENT,
  `reservation_id` int NOT NULL,
  `cancellation_reason` text,
  `refund_amount` decimal(10,2) DEFAULT '0.00',
  `cancellation_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cancellation_id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `cancellations_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cancellations`
--

LOCK TABLES `cancellations` WRITE;
/*!40000 ALTER TABLE `cancellations` DISABLE KEYS */;
/*!40000 ALTER TABLE `cancellations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `checkins`
--

DROP TABLE IF EXISTS `checkins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `checkins` (
  `checkin_id` int NOT NULL AUTO_INCREMENT,
  `guest_name` varchar(100) NOT NULL,
  `contact` varchar(15) NOT NULL,
  `id_proof` varchar(50) NOT NULL,
  `room_number` varchar(10) NOT NULL,
  `checkin_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `checkout_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`checkin_id`),
  KEY `room_number` (`room_number`),
  CONSTRAINT `checkins_ibfk_1` FOREIGN KEY (`room_number`) REFERENCES `rooms` (`room_number`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `checkins`
--

LOCK TABLES `checkins` WRITE;
/*!40000 ALTER TABLE `checkins` DISABLE KEYS */;
INSERT INTO `checkins` VALUES (1,'himesj','7458562445','adhar','R105','2025-02-21 17:26:43',NULL),(2,'vaibhav khandelwal','7453962114','ab123','R103','2025-02-22 06:52:19',NULL),(3,'vaibhav khandelwal','7453962114','adahar','R108','2025-06-10 14:07:03',NULL);
/*!40000 ALTER TABLE `checkins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discounts`
--

DROP TABLE IF EXISTS `discounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discounts` (
  `discount_id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `discount_percent` decimal(5,2) DEFAULT NULL,
  `expiry_date` date NOT NULL,
  PRIMARY KEY (`discount_id`),
  UNIQUE KEY `code` (`code`),
  CONSTRAINT `discounts_chk_1` CHECK ((`discount_percent` between 0 and 100))
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discounts`
--

LOCK TABLES `discounts` WRITE;
/*!40000 ALTER TABLE `discounts` DISABLE KEYS */;
INSERT INTO `discounts` VALUES (1,'DISC10',10.00,'2025-03-31'),(2,'SAVE15',15.00,'2025-04-30'),(3,'SUMMER20',20.00,'2025-05-15'),(4,'HOLIDAY25',25.00,'2025-06-15'),(5,'WINTER30',30.00,'2025-07-01'),(6,'NEWYEAR5',5.00,'2025-12-31'),(7,'FESTIVAL10',10.00,'2025-09-30'),(8,'OFFER15',15.00,'2025-08-15'),(9,'DISCOUNT20',20.00,'2025-10-01'),(10,'EASTER30',30.00,'2025-04-10'),(11,'FLASH50',50.00,'2025-02-28'),(12,'BLACKFRIDAY40',40.00,'2025-11-30'),(13,'VIP5',5.00,'2025-05-01'),(14,'LIMITED10',10.00,'2025-07-15'),(15,'SPRING15',15.00,'2025-05-10'),(16,'GIFT20',20.00,'2025-08-01'),(17,'THANKS25',25.00,'2025-06-10'),(18,'LOYALTY30',30.00,'2025-09-01'),(19,'EXTRA5',5.00,'2025-03-10'),(20,'WINNER10',10.00,'2025-04-20');
/*!40000 ALTER TABLE `discounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emergency_logs`
--

DROP TABLE IF EXISTS `emergency_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emergency_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `room_id` int DEFAULT NULL,
  `incident_type` enum('Fire','Medical','Security') NOT NULL,
  `description` text NOT NULL,
  `reported_by` int NOT NULL,
  `report_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `room_id` (`room_id`),
  KEY `reported_by` (`reported_by`),
  CONSTRAINT `emergency_logs_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE SET NULL,
  CONSTRAINT `emergency_logs_ibfk_2` FOREIGN KEY (`reported_by`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emergency_logs`
--

LOCK TABLES `emergency_logs` WRITE;
/*!40000 ALTER TABLE `emergency_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `emergency_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_reservations`
--

DROP TABLE IF EXISTS `group_reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_reservations` (
  `group_id` int NOT NULL AUTO_INCREMENT,
  `guest_id` int NOT NULL,
  `total_rooms` int NOT NULL,
  `check_in` date NOT NULL,
  `check_out` date NOT NULL,
  `total_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  KEY `guest_id` (`guest_id`),
  CONSTRAINT `group_reservations_ibfk_1` FOREIGN KEY (`guest_id`) REFERENCES `guests` (`guest_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_reservations`
--

LOCK TABLES `group_reservations` WRITE;
/*!40000 ALTER TABLE `group_reservations` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guests`
--

DROP TABLE IF EXISTS `guests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `guests` (
  `guest_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `contact_no` varchar(15) NOT NULL,
  `address` text,
  PRIMARY KEY (`guest_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `contact_no` (`contact_no`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guests`
--

LOCK TABLES `guests` WRITE;
/*!40000 ALTER TABLE `guests` DISABLE KEYS */;
INSERT INTO `guests` VALUES (7,'vaibhav khandelwal','vaibhavkhandelwal2408@gmail.com','7453962114','36 Tyagi Road'),(8,'amit','amitmishra24@gmail.com','5896321474','55 gyb hh'),(9,'Krishna Singhal','krishnasinghal2025@gmail.com','7037121213','roorkee abcd'),(10,'himesj','hgihbwhdg5@hkhhhs.com','7458562445','markeet road'),(11,'amit trivedi','amit1122@gmail.com','7458963212','24 gandhi nagar');
/*!40000 ALTER TABLE `guests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `housekeeping`
--

DROP TABLE IF EXISTS `housekeeping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `housekeeping` (
  `task_id` int NOT NULL AUTO_INCREMENT,
  `room_id` int NOT NULL,
  `assigned_staff` int NOT NULL,
  `task_description` text NOT NULL,
  `status` enum('Pending','Completed') DEFAULT 'Pending',
  `scheduled_date` date NOT NULL,
  PRIMARY KEY (`task_id`),
  KEY `room_id` (`room_id`),
  KEY `assigned_staff` (`assigned_staff`),
  CONSTRAINT `housekeeping_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE CASCADE,
  CONSTRAINT `housekeeping_ibfk_2` FOREIGN KEY (`assigned_staff`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `housekeeping`
--

LOCK TABLES `housekeeping` WRITE;
/*!40000 ALTER TABLE `housekeeping` DISABLE KEYS */;
INSERT INTO `housekeeping` VALUES (61,1,72,'Clean the room and change the bedsheets','Pending','2025-02-20'),(62,2,73,'Replace towels and clean bathroom','Pending','2025-02-21'),(63,3,74,'Vacuum the floor and dust furniture','Pending','2025-02-22'),(64,4,75,'Restock toiletries and clean windows','Pending','2025-02-23'),(65,5,76,'Check room lights and air conditioning','Pending','2025-02-24'),(66,6,77,'Replenish minibar and clean mirrors','Pending','2025-02-25'),(67,7,78,'Dust the furniture and vacuum carpets','Pending','2025-02-26'),(68,8,79,'Mop the floor and clean the bathroom sink','Pending','2025-02-27'),(69,9,80,'Check room security systems and replace bulbs','Pending','2025-02-28'),(70,10,81,'Organize the closet and clean the TV screen','Pending','2025-03-01'),(71,11,82,'Wash the curtains and clean the coffee table','Pending','2025-03-02'),(72,12,83,'Clean air vents and replace bedding','Pending','2025-03-03'),(73,13,84,'Polish furniture and disinfect high-touch areas','Pending','2025-03-04'),(74,14,85,'Wipe down the walls and replace bathroom mats','Pending','2025-03-05'),(75,15,86,'Check plumbing and clean floors','Pending','2025-03-06'),(76,16,87,'Restock mini bar and check room phone functionality','Pending','2025-03-07'),(77,17,88,'Clean the windows and dust the shelves','Pending','2025-03-08'),(78,18,89,'Replace linens and wipe down counters','Pending','2025-03-09'),(79,19,90,'Vacuum carpets and clean kitchen surfaces','Pending','2025-03-10'),(80,20,91,'Replace towels and scrub bathroom floor','Pending','2025-03-11'),(81,21,92,'Clean the room and change the bedsheets','Pending','2025-03-12'),(82,22,93,'Replace towels and clean bathroom','Pending','2025-03-13'),(83,23,94,'Vacuum the floor and dust furniture','Pending','2025-03-14'),(84,24,95,'Restock toiletries and clean windows','Pending','2025-03-15'),(85,25,96,'Check room lights and air conditioning','Pending','2025-03-16'),(86,26,97,'Replenish minibar and clean mirrors','Pending','2025-03-17'),(87,27,98,'Dust the furniture and vacuum carpets','Pending','2025-03-18'),(88,28,99,'Mop the floor and clean the bathroom sink','Pending','2025-03-19'),(89,29,72,'Check room security systems and replace bulbs','Pending','2025-03-20'),(90,30,73,'Organize the closet and clean the TV screen','Pending','2025-03-21'),(91,31,74,'Wash the curtains and clean the coffee table','Pending','2025-03-22'),(92,32,75,'Clean air vents and replace bedding','Pending','2025-03-23'),(93,33,76,'Polish furniture and disinfect high-touch areas','Pending','2025-03-24'),(94,34,77,'Wipe down the walls and replace bathroom mats','Pending','2025-03-25'),(95,35,78,'Check plumbing and clean floors','Pending','2025-03-26'),(96,36,79,'Restock mini bar and check room phone functionality','Pending','2025-03-27'),(97,37,80,'Clean the windows and dust the shelves','Pending','2025-03-28'),(98,38,81,'Replace linens and wipe down counters','Pending','2025-03-29'),(99,39,82,'Vacuum carpets and clean kitchen surfaces','Pending','2025-03-30'),(100,40,83,'Replace towels and scrub bathroom floor','Pending','2025-03-31'),(101,41,84,'Clean the room and change the bedsheets','Pending','2025-04-01'),(102,42,85,'Replace towels and clean bathroom','Pending','2025-04-02'),(103,43,86,'Vacuum the floor and dust furniture','Pending','2025-04-03'),(104,44,87,'Restock toiletries and clean windows','Pending','2025-04-04'),(105,45,88,'Check room lights and air conditioning','Pending','2025-04-05'),(106,46,89,'Replenish minibar and clean mirrors','Pending','2025-04-06'),(107,47,90,'Dust the furniture and vacuum carpets','Pending','2025-04-07'),(108,48,91,'Mop the floor and clean the bathroom sink','Pending','2025-04-08'),(109,49,92,'Check room security systems and replace bulbs','Pending','2025-04-09'),(110,50,93,'Organize the closet and clean the TV screen','Pending','2025-04-10');
/*!40000 ALTER TABLE `housekeeping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty_program`
--

DROP TABLE IF EXISTS `loyalty_program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty_program` (
  `loyalty_id` int NOT NULL AUTO_INCREMENT,
  `guest_id` int NOT NULL,
  `total_points` int DEFAULT '0',
  `membership_level` enum('Silver','Gold','Platinum') DEFAULT 'Silver',
  PRIMARY KEY (`loyalty_id`),
  UNIQUE KEY `guest_id` (`guest_id`),
  CONSTRAINT `loyalty_program_ibfk_1` FOREIGN KEY (`guest_id`) REFERENCES `guests` (`guest_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty_program`
--

LOCK TABLES `loyalty_program` WRITE;
/*!40000 ALTER TABLE `loyalty_program` DISABLE KEYS */;
/*!40000 ALTER TABLE `loyalty_program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `reservation_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('Cash','Credit Card','Debit Card','UPI','Net Banking') NOT NULL,
  `payment_status` enum('Pending','Completed','Failed') DEFAULT 'Completed',
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (4,6,2500.00,'Cash','Completed','2025-02-22 06:52:19'),(6,8,3200.00,'Cash','Completed','2025-06-10 14:07:03');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `guest_id` int NOT NULL,
  `room_id` int NOT NULL,
  `check_in` date NOT NULL,
  `check_out` date NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `booking_status` enum('Confirmed','Checked-in','Checked-out','Cancelled') DEFAULT 'Confirmed',
  `reservation_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`reservation_id`),
  KEY `guest_id` (`guest_id`),
  KEY `room_id` (`room_id`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`guest_id`) REFERENCES `guests` (`guest_id`) ON DELETE CASCADE,
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (4,8,1,'2025-02-21','2025-02-22',1200.00,'Checked-in','2025-02-21 14:06:51'),(5,10,5,'2025-02-21','2025-02-22',1300.00,'Checked-in','2025-02-21 17:26:43'),(6,7,3,'2025-02-22','2025-02-23',2500.00,'Checked-in','2025-02-22 06:52:19'),(8,7,8,'2025-06-10','2025-06-11',3200.00,'Checked-in','2025-06-10 14:07:03');
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `room_id` int NOT NULL AUTO_INCREMENT,
  `room_number` varchar(10) NOT NULL,
  `room_type` enum('Single','Double','Suite','Deluxe') NOT NULL,
  `price_per_night` decimal(10,2) NOT NULL,
  `status` enum('Available','Occupied','Under Maintenance') DEFAULT 'Available',
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `room_number` (`room_number`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,'R101','Single',1200.00,'Occupied'),(2,'R102','Double',1500.00,'Available'),(3,'R103','Suite',2500.00,'Occupied'),(4,'R104','Deluxe',3000.00,'Available'),(5,'R105','Single',1300.00,'Occupied'),(6,'R106','Double',1600.00,'Available'),(7,'R107','Suite',2700.00,'Available'),(8,'R108','Deluxe',3200.00,'Occupied'),(9,'R109','Single',1400.00,'Available'),(10,'R110','Double',1700.00,'Available'),(11,'R111','Suite',2800.00,'Under Maintenance'),(12,'R112','Deluxe',3300.00,'Available'),(13,'R113','Single',1500.00,'Available'),(14,'R114','Double',1800.00,'Available'),(15,'R115','Suite',2900.00,'Available'),(16,'R116','Deluxe',3400.00,'Available'),(17,'R117','Single',1600.00,'Available'),(18,'R118','Double',1900.00,'Available'),(19,'R119','Suite',3000.00,'Under Maintenance'),(20,'R120','Deluxe',3500.00,'Available'),(21,'R121','Single',1700.00,'Available'),(22,'R122','Double',2000.00,'Available'),(23,'R123','Suite',3100.00,'Available'),(24,'R124','Deluxe',3600.00,'Available'),(25,'R125','Single',1800.00,'Available'),(26,'R126','Double',2100.00,'Available'),(27,'R127','Suite',3200.00,'Available'),(28,'R128','Deluxe',3700.00,'Available'),(29,'R129','Single',1900.00,'Available'),(30,'R130','Double',2200.00,'Available'),(31,'R131','Suite',3300.00,'Available'),(32,'R132','Deluxe',3800.00,'Available'),(33,'R133','Single',2000.00,'Available'),(34,'R134','Double',2300.00,'Available'),(35,'R135','Suite',3400.00,'Available'),(36,'R136','Deluxe',3900.00,'Available'),(37,'R137','Single',2100.00,'Available'),(38,'R138','Double',2400.00,'Available'),(39,'R139','Suite',3500.00,'Available'),(40,'R140','Deluxe',4000.00,'Under Maintenance'),(41,'R141','Single',2200.00,'Available'),(42,'R142','Double',2500.00,'Available'),(43,'R143','Suite',3600.00,'Available'),(44,'R144','Deluxe',4100.00,'Available'),(45,'R145','Single',2300.00,'Available'),(46,'R146','Double',2600.00,'Available'),(47,'R147','Suite',3700.00,'Available'),(48,'R148','Deluxe',4200.00,'Available'),(49,'R149','Single',2400.00,'Available'),(50,'R150','Double',2700.00,'Available');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `special_requests`
--

DROP TABLE IF EXISTS `special_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `special_requests` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `reservation_id` int NOT NULL,
  `request_type` enum('Early Check-in','Late Check-out') NOT NULL,
  `approval_status` enum('Pending','Approved','Denied') DEFAULT 'Pending',
  PRIMARY KEY (`request_id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `special_requests_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `special_requests`
--

LOCK TABLES `special_requests` WRITE;
/*!40000 ALTER TABLE `special_requests` DISABLE KEYS */;
INSERT INTO `special_requests` VALUES (1,4,'Early Check-in','Pending');
/*!40000 ALTER TABLE `special_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `staff_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) NOT NULL,
  `role` enum('Manager','Receptionist','Housekeeping','Chef','Security') NOT NULL,
  `contact_no` varchar(15) NOT NULL,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `contact_no` (`contact_no`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (67,'Ravi Kumar','Manager','9876543210','ravi.kumar@example.com'),(68,'Anita Sharma','Manager','9887654321','anita.sharma@example.com'),(69,'Vikram Patel','Receptionist','9998765432','vikram.patel@example.com'),(70,'Priya Reddy','Receptionist','9664332111','priya.reddy@example.com'),(71,'Meera Gupta','Receptionist','9553221100','meera.gupta@example.com'),(72,'Rajesh Verma','Housekeeping','9334432211','rajesh.verma@example.com'),(73,'Neha Desai','Housekeeping','9776543210','neha.desai@example.com'),(74,'Suresh Iyer','Housekeeping','9664223344','suresh.iyer@example.com'),(75,'Asha Verma','Housekeeping','9733221101','asha.verma@example.com'),(76,'Sunil Reddy','Housekeeping','9988776656','sunil.reddy@example.com'),(77,'Rani Kapoor','Housekeeping','9675554434','rani.kapoor@example.com'),(78,'Vijay Singh','Housekeeping','9487887756','vijay.singh@example.com'),(79,'Bipin Kumar','Housekeeping','9339988778','bipin.kumar@example.com'),(80,'Pooja Gupta','Housekeeping','9988776657','pooja.gupta@example.com'),(81,'Arvind Deshmukh','Housekeeping','9877776667','arvind.deshmukh@example.com'),(82,'Komal Patel','Housekeeping','9723445567','komal.patel@example.com'),(83,'Ravi Kapoor','Housekeeping','9400123457','ravi.kapoor@example.com'),(84,'Suman Shah','Housekeeping','9884433222','suman.shah@example.com'),(85,'Bharat Yadav','Housekeeping','9833221101','bharat.yadav@example.com'),(86,'Swati Bhatia','Housekeeping','9322233445','swati.bhatia@example.com'),(87,'Arun Kumar','Housekeeping','9555666778','arun.kumar@example.com'),(88,'Kiran Singh','Housekeeping','9922334456','kiran.singh@example.com'),(89,'Raghav Mehta','Housekeeping','9223445567','raghav.mehta@example.com'),(90,'Gaurav Mehra','Housekeeping','9765443323','gaurav.mehra@example.com'),(91,'Shubham Bansal','Housekeeping','9912345679','shubham.bansal@example.com'),(92,'Nisha Gupta','Housekeeping','9000011123','nisha.gupta@example.com'),(93,'Raghav Kumar','Housekeeping','9772233446','raghav.kumar@example.com'),(94,'Amit Bansal','Housekeeping','9600112234','amit.bansal@example.com'),(95,'Neelam Reddy','Housekeeping','9100778898','neelam.reddy@example.com'),(96,'Aman Singh','Housekeeping','9700112234','aman.singh@example.com'),(97,'Ravi Joshi','Housekeeping','9600887767','ravi.joshi@example.com'),(98,'Alok Sharma','Housekeeping','9332211001','alok.sharma@example.com'),(99,'Kavita Mehta','Housekeeping','9433221112','kavita.mehta@example.com'),(100,'Deepak Yadav','Housekeeping','9999888778','deepak.yadav@example.com'),(101,'Madhuri Bhatia','Housekeeping','9400223345','madhuri.bhatia@example.com'),(102,'Rajendra Singh','Housekeeping','9776612234','rajendra.singh@example.com'),(103,'Shruti Verma','Housekeeping','9337766445','shruti.verma@example.com'),(104,'Vikash Sharma','Housekeeping','9911223345','vikash.sharma@example.com'),(105,'Simran Gupta','Housekeeping','9553221101','simran.gupta@example.com'),(106,'Pankaj Mehta','Housekeeping','9776544456','pankaj.mehta@example.com'),(107,'Ashok Kumar','Housekeeping','9772233447','ashok.kumar@example.com'),(108,'Nidhi Rani','Housekeeping','9001122333','nidhi.rani@example.com'),(109,'Krishna Yadav','Security','9111223344','krishna.yadav@example.com'),(110,'Ankit Reddy','Security','9776644321','ankit.reddy@example.com'),(111,'Ravi Bhagat','Chef','9398776655','ravi.bhagat@example.com'),(112,'Sunil Chauhan','Chef','9333224455','sunil.chauhan@example.com'),(113,'Rina Patel','Chef','9744556677','rina.patel@example.com'),(114,'Sandeep Chauhan','Chef','9611223344','sandeep.chauhan@example.com'),(115,'Pooja Sharma','Chef','9512233445','pooja.sharma@example.com'),(116,'Madhav Reddy','Chef','9623344556','madhav.reddy@example.com'),(117,'Ramesh Patel','Chef','9501122334','ramesh.patel@example.com'),(118,'Rajeev Gupta','Chef','9334455667','rajeev.gupta@example.com'),(119,'Gita Sharma','Chef','9711223345','gita.sharma@example.com');
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-10 20:27:22
