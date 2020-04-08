package com.accela.pianoforte.routes.common;

import java.time.*;

public class UTCTicks {

	public static Long getUtcTime(final OffsetDateTime dateTime) {
		return Duration.between(Instant.parse("0001-01-01T00:00:00.00Z"),
				dateTime.toInstant()).toMillis() * 10000;
	}

}
