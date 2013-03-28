SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- database: `testdb`
--

-- --------------------------------------------------------

DROP TABLE IF EXISTS `productDB`;
DROP TABLE IF EXISTS `bto`;
DROP TABLE IF EXISTS `material item`;
DROP TABLE IF EXISTS `material order`;
DROP TABLE IF EXISTS `shipment`;
DROP TABLE IF EXISTS `bill`;
DROP TABLE IF EXISTS `invoice`;
DROP TABLE IF EXISTS `receipt`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `order`;

-- --------------------------------------------------------

--
-- table structure for table `productDB`
--

CREATE TABLE IF NOT EXISTS `productDB` (
  `miid` varchar(60) NOT NULL,
  `moid` varchar(60) NOT NULL,
  PRIMARY KEY (`miid`,`moid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------



-- --------------------------------------------------------

--
-- table structure for table `bto`
--

CREATE TABLE IF NOT EXISTS `bto` (
  `btoid` varchar(60) NOT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`btoid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- data for table `bto`
--

INSERT INTO `bto` (`btoid`, `state`) VALUES
(4, 'started');

-- --------------------------------------------------------

--
-- table structure for table `material item`
--

CREATE TABLE IF NOT EXISTS `material item` (
  `miid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `moid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`miid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `material order`
--

CREATE TABLE IF NOT EXISTS `material order` (
  `moid` varchar(60) NOT NULL,
  `btoid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`moid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `shipment`
--

CREATE TABLE IF NOT EXISTS `shipment` (
  `sid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `bill`
--

CREATE TABLE IF NOT EXISTS `bill` (
  `bid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`bid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `invoice`
--

CREATE TABLE IF NOT EXISTS `invoice` (
  `iid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`iid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `order`
--

CREATE TABLE IF NOT EXISTS `order` (
  `oid` varchar(60) NOT NULL,
  `btoid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

--
-- data für table `order`
--

INSERT INTO `order` (`oid`, `state`) VALUES
(4, 'created');

INSERT INTO `order` (`oid`, `state`) VALUES
(12, 'received'),
(13, 'received');

-- --------------------------------------------------------

--
-- table structure for table `product`
--

CREATE TABLE IF NOT EXISTS `product` (
  `pid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- table structure for table `receipt`
--

CREATE TABLE IF NOT EXISTS `receipt` (
  `rid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- data for table `receipt`
--

INSERT INTO `receipt` (`rid`, `oid`, `state`) VALUES
('uuid', NULL, 'approved');

