package requestresult;

public record CreateRequest(
    String authToken,
    String gameName
) {
}
