import SwiftUI

struct OccurrencesView: View {
    @EnvironmentObject var appState: AppState
    @State private var selectedStatusFilter: String = "Todas"
    @State private var showingRegisterSheet = false
    @State private var searchText = ""
    @State private var occToToggleUrgent: Occurrence? = nil
    @State private var showToggleUrgentAlert = false

    let statusOptions = [
        ("Todas", "list.bullet"),
        ("Urgentes", "exclamationmark.triangle.fill"),
        ("Abertas", "lock.open"),
        ("Finalizadas", "checkmark.circle")
    ]

    var filteredOccurrences: [Occurrence] {
        appState.occurrences.filter { occ in
            let matchesSearch = searchText.isEmpty ||
                occ.title.localizedCaseInsensitiveContains(searchText) ||
                occ.unit.localizedCaseInsensitiveContains(searchText) ||
                occ.description.localizedCaseInsensitiveContains(searchText)

            let matchesStatus: Bool
            switch selectedStatusFilter {
            case "Urgentes": matchesStatus = occ.priority == .high
            case "Abertas": matchesStatus = (occ.status == .open || occ.status == .inProgress) && occ.priority != .high
            case "Finalizadas": matchesStatus = occ.status == .resolved
            default: matchesStatus = true
            }

            return matchesSearch && matchesStatus
        }.sorted { occ1, occ2 in
            let p1 = sortPriority(for: occ1)
            let p2 = sortPriority(for: occ2)
            if p1 != p2 {
                return p1 < p2
            }
            return occ1.date > occ2.date
        }
    }

    private func sortPriority(for occ: Occurrence) -> Int {
        if occ.priority == .high {
            return 0
        } else if occ.status == .open || occ.status == .inProgress {
            return 1
        } else if occ.status == .resolved {
            return 2
        } else {
            return 3
        }
    }

    var body: some View {
        VStack {
            // Segmented Control
            Picker("Filtro", selection: $selectedStatusFilter) {
                ForEach(statusOptions, id: \.0) { option in
                    Image(systemName: option.1).tag(option.0)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
            .padding(.top, 8)

            List {
                ForEach(filteredOccurrences) { occurrence in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(occurrence.title)
                                .font(.headline)
                            Spacer()
                            Text(occurrence.priority.rawValue)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(priorityColor(occurrence.priority).opacity(0.15))
                                .foregroundColor(priorityColor(occurrence.priority))
                                .cornerRadius(6)
                        }

                        Text(occurrence.description)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(2)

                        HStack {
                            Label(occurrence.unit, systemImage: "house")
                                .font(.caption)
                                .foregroundColor(.blue)

                            Spacer()

                            Label(occurrence.status.rawValue, systemImage: "clock")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.vertical, 6)
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            appState.occurrences.removeAll { $0.id == occurrence.id }
                        } label: {
                            Label("Excluir", systemImage: "trash")
                        }

                        Button {
                            occToToggleUrgent = occurrence
                            showToggleUrgentAlert = true
                        } label: {
                            Label(occurrence.priority == .high ? "Normalizar" : "Priorizar", systemImage: "exclamationmark.triangle")
                        }
                        .tint(occurrence.priority == .high ? .orange : .red)

                        Button {
                            if let idx = appState.occurrences.firstIndex(where: { $0.id == occurrence.id }) {
                                appState.occurrences[idx].status = .resolved
                            }
                        } label: {
                            Label("Concluir", systemImage: "checkmark.circle")
                        }
                        .tint(.green)
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
        .searchable(text: $searchText, prompt: "Pesquisar ocorrências")
        .navigationTitle("Ocorrências Operacionais")
        .toolbar {
            Button {
                showingRegisterSheet = true
            } label: {
                Image(systemName: "plus")
            }
        }
        .sheet(isPresented: $showingRegisterSheet) {
            RegisterOccurrenceView()
        }
        .alert(
            occToToggleUrgent?.priority == .high ? "Remover Urgência" : "Confirmar Urgência",
            isPresented: $showToggleUrgentAlert,
            presenting: occToToggleUrgent
        ) { targetOcc in
            Button(targetOcc.priority == .high ? "Remover Urgência" : "Confirmar Urgência", role: targetOcc.priority == .high ? .destructive : .none) {
                if let idx = appState.occurrences.firstIndex(where: { $0.id == targetOcc.id }) {
                    appState.occurrences[idx].priority = targetOcc.priority == .high ? .medium : .high
                }
            }
            Button("Cancelar", role: .cancel) {}
        } message: { targetOcc in
            Text(targetOcc.priority == .high ?
                 "Deseja remover a prioridade de urgência da ocorrência '\(targetOcc.title)'?" :
                 "Tem certeza de que deseja destacar a ocorrência '\(targetOcc.title)' como URGENTE?")
        }
    }

    private func priorityColor(_ priority: Priority) -> Color {
        switch priority {
        case .high: return .red
        case .medium: return .orange
        case .low: return .blue
        }
    }
}

// MARK: - Register Occurrence Form View
struct RegisterOccurrenceView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState

    @State private var title = ""
    @State private var description = ""
    @State private var unit = ""
    @State private var category = "Geral"
    @State private var priority: Priority = .medium
    @State private var showConfirmUrgentAlert = false

    let categories = ["Geral", "Sossego", "Manutenção", "Iluminação", "Limpeza", "Segurança", "Garagem"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Informações Principais") {
                    TextField("Título da Ocorrência", text: $title)
                    TextField("Unidade / Local (ex: 102-A ou Portaria)", text: $unit)
                    Picker("Categoria", selection: $category) {
                        ForEach(categories, id: \.self) { cat in
                            Text(cat).tag(cat)
                        }
                    }
                }

                Section("Detalhes e Urgência") {
                    Picker("Prioridade", selection: $priority) {
                        ForEach(Priority.allCases, id: \.self) { prio in
                            Text(prio.rawValue).tag(prio)
                        }
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: priority) { newValue in
                        if newValue == .high {
                            showConfirmUrgentAlert = true
                        }
                    }

                    TextEditor(text: $description)
                        .frame(height: 100)
                }
            }
            .navigationTitle("Registrar Ocorrência")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Cadastrar") {
                        if !title.isEmpty && !unit.isEmpty {
                            if priority == .high && !showConfirmUrgentAlert {
                                showConfirmUrgentAlert = true
                            } else {
                                performRegister()
                            }
                        }
                    }
                }
            }
            .alert("Confirmar Urgência", isPresented: $showConfirmUrgentAlert) {
                Button("Confirmar Urgência") {
                    performRegister()
                }
                Button("Cancelar", role: .cancel) {
                    priority = .medium
                }
            } message: {
                Text("Deseja confirmar o cadastro desta ocorrência com prioridade alta (URGENTE)?")
            }
        }
    }

    private func performRegister() {
        let newOcc = Occurrence(
            title: title,
            description: description.isEmpty ? "Sem descrição" : description,
            unit: unit,
            date: Date(),
            priority: priority,
            status: .open,
            category: category
        )
        appState.occurrences.insert(newOcc, at: 0)
        dismiss()
    }
}

