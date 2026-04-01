package backend.playsembly.domain;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface AppUserRepository extends CrudRepository<AppUser, Long>{
    AppUser findByUsername (String username);
    boolean existsByUsername (String username);
}
