package com.accela.pianoforte.routes.common;

import java.time.Duration;
import java.time.Instant;

public class UTCTicks {

	public static Long getUtcTime(final Instant instant) {
		return Duration.between(Instant.parse("0001-01-01T00:00:00.00Z"), instant).toMillis() * 10000;
	}

}
