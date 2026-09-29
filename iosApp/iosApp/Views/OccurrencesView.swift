import SwiftUI

struct OccurrencesView: View {
    @EnvironmentObject var appState: AppState
    @State private var selectedStatusFilter: String = "Todas"
    @State private var showingRegisterSheet = false
    @State private var searchText = ""

    let statusOptions = ["Todas", "Abertas", "Em Andamento", "Concluídas"]

    var filteredOccurrences: [Occurrence] {
        appState.occurrences.filter { occ in
            let matchesSearch = searchText.isEmpty ||
                occ.title.localizedCaseInsensitiveContains(searchText) ||
                occ.unit.localizedCaseInsensitiveContains(searchText) ||
                occ.description.localizedCaseInsensitiveContains(searchText)

            let matchesStatus: Bool
            switch selectedStatusFilter {
            case "Abertas": matchesStatus = occ.status == .open
            case "Em Andamento": matchesStatus = occ.status == .inProgress
            case "Concluídas": matchesStatus = occ.status == .resolved
            default: matchesStatus = true
            }

            return matchesSearch && matchesStatus
        }
    }

    var body: some View {
        VStack {
            // Segmented Control
            Picker("Filtro", selection: $selectedStatusFilter) {
                ForEach(statusOptions, id: \.self) { option in
                    Text(option).tag(option)
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
                            Label(occurrence.unit, systemName: "house")
                                .font(.caption)
                                .foregroundColor(.blue)

                            Spacer()

                            Label(occurrence.status.rawValue, systemName: "clock")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.vertical, 6)
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            appState.occurrences.removeAll { $0.id == occurrence.id }
                        } label: {
                            Label("Excluir", systemName: "trash")
                        }

                        Button {
                            if let idx = appState.occurrences.firstIndex(where: { $0.id == occurrence.id }) {
                                appState.occurrences[idx].status = .resolved
                            }
                        } label: {
                            Label("Concluir", systemName: "checkmark.circle")
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
                }
            }
        }
    }
}
