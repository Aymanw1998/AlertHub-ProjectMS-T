package com.mst.dto.github;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubResponseDTO {
    private String name;
    private String path;
    private String type;
    private String download_url;
    private String url;

}
