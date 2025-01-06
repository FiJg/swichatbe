package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.exceptions.HashException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class HashService {
	private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
	
	public String hash(@NonNull String string) throws HashException {
		char[] chars = string.toCharArray();
		
		try {
			String hash = argon2.hash(10, 65536, 1, chars);
			
			if (argon2.verify(hash, chars)) {
				return hash;
			} else {
				throw new HashException("Hash verification failed");
			}
		} finally {
			argon2.wipeArray(chars);
		}
	}
	
	public boolean verify(@NonNull String hash, @NonNull String string) {
		char[] chars = string.toCharArray();
		
		try {
			return argon2.verify(hash, chars);
		} finally {
			argon2.wipeArray(chars);
		}
	}
}
