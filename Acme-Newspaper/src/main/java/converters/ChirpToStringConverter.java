package converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import domain.Chirp;

@Component
@Transactional
public class ChirpToStringConverter implements Converter<Chirp, String> {
	
	@Override
	public String convert(final Chirp res) {
		String result;

		if (res == null)
			result = null;
		else
			result = String.valueOf(res.getId());

		return result;
	}
}
