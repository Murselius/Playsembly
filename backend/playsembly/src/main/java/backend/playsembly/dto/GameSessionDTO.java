package backend.playsembly.dto;

import java.time.LocalDateTime;

import backend.playsembly.domain.GameSession;

public class GameSessionDTO {
    private Long id;
    private String description;
    private int minPlayers;
    private int maxPlayers;
    private int estimatedDuration;
    private LocalDateTime startTime;
    private BoardGameDTO boardgame;
    private AppUserDTO creator;


    public GameSessionDTO(Long id, String description, int minPlayers, int maxPlayers, int estimatedDuration,
            LocalDateTime startTime, BoardGameDTO boardgame, AppUserDTO creator) {
        this.id = id;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.estimatedDuration = estimatedDuration;
        this.startTime = startTime;
        this.boardgame = boardgame;
        this.creator = creator;
    }

    public GameSessionDTO() {
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BoardGameDTO getBoardgame() {
        return boardgame;
    }

    public void setBoardgame(BoardGameDTO boardgame) {
        this.boardgame = boardgame;
    }

    public AppUserDTO getCreator() {
        return creator;
    }

    public void setCreator(AppUserDTO creator) {
        this.creator = creator;
    }

    //konstruktori GameSessionista DTO:ksi
    public GameSessionDTO(GameSession game) {
        this.id = game.getId();
        this.description = game.getDescription();
        this.estimatedDuration = game.getEstimatedDuration();
        this.minPlayers = game.getMinPlayers();
        this.maxPlayers = game.getMaxPlayers();
        this.startTime = game.getStartTime();

        BoardGameDTO bg = new BoardGameDTO();
        bg.setBggId(game.getBoardgame().getBggId());
        bg.setName(game.getBoardgame().getName());
        bg.setImageUrl(game.getBoardgame().getImageUrl());
        bg.setMinPlayers(game.getBoardgame().getMinPlayers());
        bg.setMaxPlayers(game.getBoardgame().getMaxPlayers());
        this.boardgame = bg;

        AppUserDTO creatorDto = new AppUserDTO();
        creatorDto.setId(game.getCreator().getId());
        creatorDto.setUsername(game.getCreator().getUsername());
        this.creator = creatorDto;
    }

    public static class BoardGameDTO {
        private Long bggId;
        private String name;
        private String imageUrl;
        private int minPlayers;
        private int maxPlayers;

        public Long getBggId() {
            return bggId;
        }
        public void setBggId(Long bggId) {
            this.bggId = bggId;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getImageUrl() {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        public int getMinPlayers() {
            return minPlayers;
        }
        public void setMinPlayers(int minPlayers) {
            this.minPlayers = minPlayers;
        }
        public int getMaxPlayers() {
            return maxPlayers;
        }
        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        

    }

    public static class AppUserDTO {
        private Long id;
        private String username;

        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }

        
    }

}
