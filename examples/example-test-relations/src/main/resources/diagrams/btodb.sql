SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- database: `testdb`
--

-- --------------------------------------------------------

DROP TABLE IF EXISTS `ProC`;
DROP TABLE IF EXISTS `CO`;
DROP TABLE IF EXISTS `PO`;
DROP TABLE IF EXISTS `CP`;
DROP TABLE IF EXISTS `B`;

-- --------------------------------------------------------

--
-- table structure for table `ProC`
--

CREATE TABLE IF NOT EXISTS `ProC` (
  `procid` varchar(60) NOT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`procid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- table structure for table `CO`
--

CREATE TABLE IF NOT EXISTS `CO` (
  `coid` varchar(60) NOT NULL,
  `procid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`coid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Data for table `ProC`
--

INSERT INTO `CO` (`coid`, `procid`, `state`) VALUES
(12, NULL, 'created');
INSERT INTO `CO` (`coid`, `procid`, `state`) VALUES
(215, NULL, 'created');
INSERT INTO `CO` (`coid`, `procid`, `state`) VALUES
(31, NULL, 'created');
INSERT INTO `CO` (`coid`, `procid`, `state`) VALUES
(3, NULL, 'created');

-- --------------------------------------------------------

--
-- table structure for table `PO`
--

CREATE TABLE IF NOT EXISTS `PO` (
  `poid` varchar(60) NOT NULL,
  `procid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`poid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `CP`
--

CREATE TABLE IF NOT EXISTS `CP` (
  `cpid` varchar(60) NOT NULL,
  `coid` varchar(60) DEFAULT NULL,
  `poid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`cpid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `B`
--

CREATE TABLE IF NOT EXISTS `B` (
  `bid` varchar(60) NOT NULL,
  `poid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`bid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

