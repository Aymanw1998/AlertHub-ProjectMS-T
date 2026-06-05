package com.mst.dto.github;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubContentDTO {
    private String name;
    private String path;
    private String type;
    private String download_url;

}
