package at.technikum_wien.swen3.paperless.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "access-stats")
public class AccessStatisticsConfig {

    private String inputDir = "/access-stats";

    private String archiveDir = "/access-stats/archive";

    private String filePattern = "access-stats-.*\\.xml";

    private String cronSchedule = "0 0 1 * * ?";
}
