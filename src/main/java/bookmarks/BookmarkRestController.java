package bookmarks;

import java.net.URI;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import bookmarks.model.Bookmark;
import bookmarks.repository.AccountRepository;
import bookmarks.repository.BookmarkRepository;


@RestController
@RequestMapping("{userId}/bookmarks")
class BookmarkRestController {

	private BookmarkRepository bookmarkRepository;

	private AccountRepository accountRepository;

	@Autowired
	BookmarkRestController(BookmarkRepository bookmarkRepository, AccountRepository accountRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.accountRepository = accountRepository;
	}

	@PostMapping
	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark bookmark) {
		this.validateUser(userId);

		return this.accountRepository
				.findByUsername(userId).map(account -> {
					Bookmark result = bookmarkRepository
					.save(new Bookmark(account, bookmark.getUri(), bookmark.getDescription()));
					URI location = ServletUriComponentsBuilder
							.fromCurrentRequest().path("/{id}")
							.buildAndExpand(result.getId()).toUri();
					
				return ResponseEntity.created(location).build();
		})
		 .orElse(ResponseEntity.noContent().build());
	}
	
	@GetMapping("/{bookmarkId}")
	Bookmark readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
		this.validateUser(userId);
		
		return this.bookmarkRepository.findOne(bookmarkId);
	}
	
	@GetMapping
	Collection<Bookmark> readBookmarks(@PathVariable String userId) {
		this.validateUser(userId);
		
		return this.bookmarkRepository.findByAccountUsername(userId);
	}
	
	
	
	private void validateUser(String userId) {
		this.accountRepository.findByUsername(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}
}
