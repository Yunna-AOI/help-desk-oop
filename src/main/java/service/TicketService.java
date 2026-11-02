package service;

import model.Attachment;
import model.Category;
import model.Department;
import model.SlaPolicy;
import model.Ticket;
import model.TicketAssignmentHistory;
import model.TicketComment;
import model.TicketDetails;
import model.TicketPriority;
import model.TicketStatus;
import model.TicketStatusHistory;
import model.User;
import model.UserRole;
import repository.AssignmentHistoryRepository;
import repository.AttachmentRepository;
import repository.AuditRepository;
import repository.CategoryRepository;
import repository.CommentRepository;
import repository.DepartmentRepository;
import repository.ReportRepository;
import repository.SlaPolicyRepository;
import repository.StatusHistoryRepository;
import repository.TagRepository;
import repository.TicketRepository;
import repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final CommentRepository commentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final TagRepository tagRepository;
    private final ReportRepository reportRepository;
    private final AuditRepository auditRepository;
    private final NotificationService notificationService;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         DepartmentRepository departmentRepository,
                         CommentRepository commentRepository,
                         StatusHistoryRepository statusHistoryRepository,
                         AssignmentHistoryRepository assignmentHistoryRepository,
                         AttachmentRepository attachmentRepository,
                         SlaPolicyRepository slaPolicyRepository,
                         TagRepository tagRepository,
                         ReportRepository reportRepository,
                         AuditRepository auditRepository,
                         NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.commentRepository = commentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.tagRepository = tagRepository;
        this.reportRepository = reportRepository;
        this.auditRepository = auditRepository;
        this.notificationService = notificationService;
    }

    public Ticket createTicket(String title, String description, TicketPriority priority,
                               long customerId, Long categoryId) {
        if (title == null || title.isBlank() || title.trim().length() < 3) {
            throw new IllegalArgumentException("Тема заявки должна содержать минимум 3 символа");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Описание заявки не может быть пустым");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден: " + customerId));
        if (!customer.isActive()) {
            throw new IllegalStateException("Клиент деактивирован");
        }

        TicketPriority effectivePriority = priority;
        LocalDateTime dueAt = null;

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));
            if (!category.isActive()) {
                throw new IllegalStateException("Нельзя создавать заявку в неактивной категории");
            }
            if (effectivePriority == null) {
                effectivePriority = category.getDefaultPriority();
            }
        }

        if (effectivePriority == null) {
            effectivePriority = TicketPriority.MEDIUM;
        }

        Optional<SlaPolicy> policy = slaPolicyRepository.findByPriority(effectivePriority);
        if (policy.isPresent()) {
            dueAt = LocalDateTime.now().plusMinutes(policy.get().getResolveMinutes());
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(title.trim());
        ticket.setDescription(description.trim());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setPriority(effectivePriority);
        ticket.setCustomerId(customerId);
        ticket.setCategoryId(categoryId);
        ticket.setDueAt(dueAt);

        Ticket saved = ticketRepository.save(ticket);
        auditRepository.log(customerId, "CREATE_TICKET", "TICKET", saved.getId(), saved.getTitle());
        notificationService.send("Создана заявка N" + saved.getId() + ": " + saved.getTitle());
        return saved;
    }

    public Ticket createTicket(String title, String description, long customerId, Long categoryId) {
        return createTicket(title, description, null, customerId, categoryId);
    }

    public Optional<Ticket> findTicket(long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> findTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> filterTickets(TicketStatus status, TicketPriority priority) {
        return ticketRepository.findByStatusAndPriority(status, priority);
    }

    public List<Ticket> findTicketsByCustomer(long customerId) {
        return ticketRepository.findByCustomerId(customerId);
    }

    public List<Ticket> findTicketPage(int page, int size) {
        return ticketRepository.findPage(page, size);
    }

    public List<Ticket> searchTickets(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Строка поиска не может быть пустой");
        }
        return ticketRepository.search(text.trim());
    }

    public List<Ticket> findOverdueTickets() {
        return ticketRepository.findOverdue();
    }

    public List<TicketDetails> findTicketDetails() {
        return ticketRepository.findDetails();
    }

    public TicketDetails getTicketCard(long ticketId) {
        return ticketRepository.findDetailsById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + ticketId));
    }

    public void changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId) {
        userRepository.findById(changedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + changedByUserId));

        ticketRepository.changeStatus(ticketId, newStatus, changedByUserId);
        notificationService.send("Заявка N" + ticketId + " переведена в статус " + newStatus);
    }

    public void assignTicket(long ticketId, long agentId, long changedByUserId) {
        userRepository.findById(changedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + changedByUserId));

        ticketRepository.assign(ticketId, agentId, changedByUserId);
        notificationService.send("Заявка N" + ticketId + " назначена сотруднику " + agentId);
    }

    public TicketComment addComment(long ticketId, long authorId, String text, boolean internal) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Комментарий не может быть пустым");
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Автор не найден: " + authorId));

        TicketComment comment = new TicketComment(ticketId, authorId, text.trim(), internal);
        TicketComment saved = commentRepository.add(comment);
        saved.setAuthorName(author.getName());
        saved.setAuthorRole(author.getRole());
        notificationService.send("Добавлен комментарий к заявке N" + ticketId);
        return saved;
    }

    public List<TicketComment> getComments(long ticketId) {
        return commentRepository.findByTicketId(ticketId);
    }

    public List<TicketComment> getExternalComments(long ticketId) {
        return commentRepository.findExternalByTicketId(ticketId);
    }

    public List<TicketStatusHistory> getStatusHistory(long ticketId) {
        return statusHistoryRepository.findByTicketId(ticketId);
    }

    public List<TicketAssignmentHistory> getAssignmentHistory(long ticketId) {
        return assignmentHistoryRepository.findByTicketId(ticketId);
    }

    public Attachment addAttachment(long ticketId, long uploadedById, String originalName,
                                    String storagePath, String contentType, long sizeBytes) {
        Attachment attachment = new Attachment(ticketId, uploadedById, originalName,
                storagePath, contentType, sizeBytes);
        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getAttachments(long ticketId) {
        return attachmentRepository.findByTicketId(ticketId);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserById(long id) {
        return userRepository.findById(id);
    }

    public List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findCategoryById(long id) {
        return categoryRepository.findById(id);
    }

    public void deactivateCategory(long id) {
        if (!categoryRepository.deactivate(id)) {
            throw new IllegalArgumentException("Категория не найдена: " + id);
        }
    }

    public List<Department> findAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<SlaPolicy> findAllSlaPolicies() {
        return slaPolicyRepository.findAll();
    }

    public void addTag(long ticketId, String tagName) {
        tagRepository.addTag(ticketId, tagName);
    }

    public List<String> getTags(long ticketId) {
        return tagRepository.findTagsByTicketId(ticketId);
    }

    public List<Ticket> findTicketsByTag(String tagName) {
        return tagRepository.findByTag(tagName);
    }

    public Map<String, Long> reportByStatus() {
        return reportRepository.countByStatus();
    }

    public Map<String, Long> reportByCategory() {
        return reportRepository.countByCategory();
    }

    public List<model.AgentStats> reportAgentStats() {
        return reportRepository.agentStats();
    }

    public Map<String, String> reportAverageResolution() {
        return reportRepository.averageResolutionByCategory();
    }

    public List<String> recentAudit(int limit) {
        return auditRepository.findRecent(limit);
    }

    public void printTicketCard(long ticketId) {
        System.out.println(getTicketCard(ticketId));
    }
}
