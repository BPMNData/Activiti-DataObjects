SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Datenbank: `testdb`
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
-- Tabellenstruktur für Tabelle `productDB`
--

CREATE TABLE IF NOT EXISTS `productDB` (
  `miid` varchar(60) NOT NULL,
  `moid` varchar(60) NOT NULL,
  PRIMARY KEY (`miid`,`moid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------



-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `bto`
--

CREATE TABLE IF NOT EXISTS `bto` (
  `btoid` varchar(60) NOT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`btoid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `bto`
--

INSERT INTO `bto` (`btoid`, `state`) VALUES
(4, 'started');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `material item`
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
-- Tabellenstruktur für Tabelle `material order`
--

CREATE TABLE IF NOT EXISTS `material order` (
  `moid` varchar(60) NOT NULL,
  `btoid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`moid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `shipment`
--

CREATE TABLE IF NOT EXISTS `shipment` (
  `sid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `bill`
--

CREATE TABLE IF NOT EXISTS `bill` (
  `bid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`bid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `invoice`
--

CREATE TABLE IF NOT EXISTS `invoice` (
  `iid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`iid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `order`
--

CREATE TABLE IF NOT EXISTS `order` (
  `oid` varchar(60) NOT NULL,
  `btoid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `order`
--

INSERT INTO `order` (`oid`, `state`) VALUES
(4, 'created');

INSERT INTO `order` (`oid`, `state`) VALUES
(12, 'received'),
(13, 'received');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `product`
--

CREATE TABLE IF NOT EXISTS `product` (
  `pid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `receipt`
--

CREATE TABLE IF NOT EXISTS `receipt` (
  `rid` varchar(60) NOT NULL,
  `oid` varchar(60) DEFAULT NULL,
  `state` text NOT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `receipt`
--

INSERT INTO `receipt` (`rid`, `oid`, `state`) VALUES
('uuid', NULL, 'approved');

