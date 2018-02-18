package bookmarks.repository;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import bookmarks.model.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> 
{
	Collection<Bookmark> findByAccountUsername(String username);
}
