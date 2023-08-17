-- MySQL dump 10.13  Distrib 5.7.42, for Linux (x86_64)
--
-- Host: localhost    Database: iotproject
-- ------------------------------------------------------
-- Server version	5.7.42-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `actuators`
--

DROP TABLE IF EXISTS `actuators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actuators` (
  `ip` varchar(255) DEFAULT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actuators`
--

LOCK TABLES `actuators` WRITE;
/*!40000 ALTER TABLE `actuators` DISABLE KEYS */;
INSERT INTO `actuators` VALUES ('fd00:0:0:0:f6ce:36b5:cf85:293','temperature','2','2023-08-17 13:46:16'),('fd00:0:0:0:f6ce:36fe:b4b1:4d36','humidity','2','2023-08-17 13:46:16'),('fd00:0:0:0:f6ce:3612:933e:6ed1','co2','2','2023-08-17 13:46:17'),('fd00:0:0:0:f6ce:36fe:b4b1:4d36','humidity','0','2023-08-17 13:46:47'),('fd00:0:0:0:f6ce:36b5:cf85:293','temperature','0','2023-08-17 13:46:49'),('fd00:0:0:0:f6ce:3612:933e:6ed1','co2','0','2023-08-17 13:46:49'),('fd00:0:0:0:f6ce:36fe:b4b1:4d36','humidity','0','2023-08-17 13:47:22'),('fd00:0:0:0:f6ce:36b5:cf85:293','temperature','0','2023-08-17 13:47:24'),('fd00:0:0:0:f6ce:3612:933e:6ed1','co2','0','2023-08-17 13:47:24'),('fd00:0:0:0:f6ce:36b5:cf85:293','temperature','2','2023-08-17 13:47:26'),('fd00:0:0:0:f6ce:36fe:b4b1:4d36','humidity','2','2023-08-17 13:47:26'),('fd00:0:0:0:f6ce:3612:933e:6ed1','co2','2','2023-08-17 13:47:26'),('fd00:0:0:0:f6ce:36b5:cf85:293','temperature','1','2023-08-17 13:47:33'),('fd00:0:0:0:f6ce:36fe:b4b1:4d36','humidity','2','2023-08-17 13:47:34'),('fd00:0:0:0:f6ce:3612:933e:6ed1','co2','2','2023-08-17 13:47:34'),('fd00:0:0:0:f6ce:36b5:cf85:293','temperature','2','2023-08-17 13:47:41'),('fd00:0:0:0:f6ce:36fe:b4b1:4d36','humidity','2','2023-08-17 13:47:41'),('fd00:0:0:0:f6ce:3612:933e:6ed1','co2','2','2023-08-17 13:47:41');
/*!40000 ALTER TABLE `actuators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensors`
--

DROP TABLE IF EXISTS `sensors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensors` (
  `topic` varchar(255) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `value` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensors`
--

LOCK TABLES `sensors` WRITE;
/*!40000 ALTER TABLE `sensors` DISABLE KEYS */;
INSERT INTO `sensors` VALUES ('temperature','2023-08-17 13:46:27',28),('co2','2023-08-17 13:46:29',220),('humidity','2023-08-17 13:46:30',51),('temperature','2023-08-17 13:46:37',31),('co2','2023-08-17 13:46:39',270),('humidity','2023-08-17 13:46:40',56),('temperature','2023-08-17 13:46:47',28),('co2','2023-08-17 13:46:49',320),('humidity','2023-08-17 13:46:50',61),('temperature','2023-08-17 13:46:57',25),('co2','2023-08-17 13:46:59',370),('humidity','2023-08-17 13:47:00',56),('temperature','2023-08-17 13:47:07',22),('co2','2023-08-17 13:47:09',420),('humidity','2023-08-17 13:47:10',51),('temperature','2023-08-17 13:47:17',19),('co2','2023-08-17 13:47:19',370),('humidity','2023-08-17 13:47:20',46),('temperature','2023-08-17 13:47:27',16),('co2','2023-08-17 13:47:29',320),('humidity','2023-08-17 13:47:30',41),('temperature','2023-08-17 13:47:37',19),('co2','2023-08-17 13:47:39',270),('humidity','2023-08-17 13:47:40',36);
/*!40000 ALTER TABLE `sensors` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-08-17 15:49:44
