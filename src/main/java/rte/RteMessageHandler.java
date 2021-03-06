package rte;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rte.classifier.SparkLibSVM;
import rte.datastructure.Graph;
import rte.graphmatching.RteConfiguration;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
* Created by areed on 5/21/15.
*/
public class RteMessageHandler  {

    private static final Logger LOG = LoggerFactory.getLogger(RteMessageHandler.class);

    public Map<UUID, RtfeConversationData> conversationDataMap = Maps.newHashMap();

    public static final RteConfiguration config = new RteConfiguration
            .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
            .nbrSimFloodingIterations(10)
            .build();

    private class RtfeConversationData {

        private final List<String> cachedStatements = Lists.newArrayList();
        private final List<Graph> cachedGraphs = Lists.newArrayList();



        private List<String> getCachedStatments() {
            return cachedStatements;
        }

        private List<Graph> getCachedGraph() {
            return cachedGraphs;
        }
    }

    public static SparkLibSVM sparkLibSVM = new SparkLibSVM();
}
