package bookmarks;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bookmarks.hateoas.BookmarkResource;
import bookmarks.model.Bookmark;
import bookmarks.repository.AccountRepository;
import bookmarks.repository.BookmarkRepository;


@RestController
@RequestMapping("{userId}/bookmarks")
public class BookmarkRestController {

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
					
				Link forOneBookmark = new BookmarkResource(result).getLink("self");
				return ResponseEntity.created(URI.create(forOneBookmark.getHref())).build();
		})
		 .orElse(ResponseEntity.noContent().build());
	}
	
	@GetMapping(value="/{bookmarkId}", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	public BookmarkResource readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
		this.validateUser(userId);
		
		return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
	}
	
	@GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	Resources<BookmarkResource> readBookmarks(@PathVariable String userId) {
		this.validateUser(userId);
		
		List<BookmarkResource> bookmarkResources = 
				this.bookmarkRepository
				.findByAccountUsername(userId)
				.stream()
				.map(BookmarkResource::new)
				.collect(Collectors.toList());
		
		return new Resources<>(bookmarkResources);
	}
	
	
	
	private void validateUser(String userId) {
		this.accountRepository.findByUsername(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}
}
