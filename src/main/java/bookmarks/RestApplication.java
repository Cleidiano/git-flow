package bookmarks;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import bookmarks.model.Account;
import bookmarks.model.Bookmark;
import bookmarks.repository.AccountRepository;
import bookmarks.repository.BookmarkRepository;

@SpringBootApplication
public class RestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
		return (evt) -> {
			Arrays.asList("jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
			 .forEach(a -> {
				Account account = accountRepository.save(new Account(a, "password"));
				bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
				bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
			});
		};
	}
}
