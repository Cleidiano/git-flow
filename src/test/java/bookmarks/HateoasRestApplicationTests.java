package bookmarks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import bookmarks.model.Account;
import bookmarks.model.Bookmark;
import bookmarks.repository.AccountRepository;
import bookmarks.repository.BookmarkRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=HateoasRestApplication.class)
@WebAppConfiguration
public class HateoasRestApplicationTests {
	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;

	private String username = "bdussault";
	
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private Account account;

	private List<Bookmark> bookmarkList = new ArrayList<>();

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converts) {
		this.mappingJackson2HttpMessageConverter = Arrays.asList(converts).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		this.bookmarkRepository.deleteAllInBatch();
		this.accountRepository.deleteAllInBatch();

		this.account = accountRepository.save(new Account(username, "password"));
		this.bookmarkList.add(
				bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + username, "A description")));
		this.bookmarkList.add(
				bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + username, "A description")));
	}

	@Test
	public void userNotFound() throws Exception {
		mockMvc.perform(
				post("/george/bookmarks")
				.content(this.toJson(new Bookmark(null, null, null)))
				.contentType(contentType))
				.andExpect(status().isNotFound());
	}
	
	@Test
    public void readSingleBookmark() throws Exception {
        mockMvc.perform(get("/" + username + "/bookmarks/"
                + this.bookmarkList.get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + username)))
                .andExpect(jsonPath("$.description", is("A description")));
    }

    @Test
    public void readBookmarks() throws Exception {
        mockMvc.perform(get("/" + username + "/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$",  hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].uri", is("http://bookmark.com/1/" + username)))
                .andExpect(jsonPath("$[0].description", is("A description")))
                .andExpect(jsonPath("$[1].id", is(this.bookmarkList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].uri", is("http://bookmark.com/2/" + username)))
                .andExpect(jsonPath("$[1].description", is("A description")));
    }

    @Test
    public void createBookmark() throws Exception {
        String bookmarkJson = toJson(new Bookmark(
                this.account, "http://spring.io", "a bookmark to the best resource for Spring news and information"));

        this.mockMvc.perform(post("/" + username + "/bookmarks")
                .contentType(contentType)
                .content(bookmarkJson))
                .andExpect(status().isCreated());
    }
	
	

	protected String toJson(Object bookmark) throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(bookmark, MediaType.APPLICATION_JSON, outputMessage);

		return outputMessage.getBodyAsString();
	}
}
