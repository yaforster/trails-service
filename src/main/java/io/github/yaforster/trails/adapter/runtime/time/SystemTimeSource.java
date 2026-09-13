package io.github.yaforster.trails.adapter.runtime.time;

import io.github.yaforster.trails.app.services.TimeSource;
import org.springframework.stereotype.Component;

@Component
public class SystemTimeSource implements TimeSource {

	@Override
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}
