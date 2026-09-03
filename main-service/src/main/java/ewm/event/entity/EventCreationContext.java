package ewm.event.entity;

import ewm.category.Category;
import ewm.user.User;

public record EventCreationContext(Category category, User initiator) {
}
