CREATE TABLE agent (
	`id`         SERIAL,
	`description` TEXT NOT NULL,
	PRIMARY KEY(`id`)
);

CREATE TABLE log_access (
	`id`            SERIAL,
	`time`          DATETIME(3) NOT NULL,
	`ip`            VARCHAR(15) NOT NULL,
	`request`       VARCHAR(2048) NOT NULL,
	`response_code` INT NOT NULL,
	`agent_id`      BIGINT UNSIGNED,
	PRIMARY KEY(`id`),
	UNIQUE (`ip`, `time`),
	FOREIGN KEY (`agent_id`) REFERENCES `agent`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE blocked_ip (
	`id` SERIAL,
	`ip` VARCHAR(15) NOT NULL,
	PRIMARY KEY(`id`),
	UNIQUE (`ip`),
	INDEX `idx_ip` (`ip`)
);