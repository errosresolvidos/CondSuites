import SwiftUI

struct UnitsView: View {
    @EnvironmentObject var appState: AppState
    @State private var searchText = ""
    @State private var selectedStatus: UnitStatus? = nil
    @State private var showingAddUnitSheet = false

    var filteredUnits: [CondUnit] {
        appState.units.filter { unit in
            let matchesSearch = searchText.isEmpty ||
                unit.number.contains(searchText) ||
                unit.block.localizedCaseInsensitiveContains(searchText) ||
                unit.residentName.localizedCaseInsensitiveContains(searchText)

            let matchesStatus = selectedStatus == nil || unit.status == selectedStatus
            return matchesSearch && matchesStatus
        }
    }

    var body: some View {
        NavigationStack {
            VStack {
                // Filter chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        FilterChip(title: "Todas", isSelected: selectedStatus == nil) {
                            selectedStatus = nil
                        }
                        ForEach(UnitStatus.allCases, id: \.self) { status in
                            FilterChip(title: status.rawValue, isSelected: selectedStatus == status) {
                                selectedStatus = status
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical, 8)

                List {
                    ForEach(filteredUnits) { unit in
                        HStack(spacing: 16) {
                            VStack {
                                Text(unit.block)
                                    .font(.caption2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.blue)
                                    .cornerRadius(4)
                                Text(unit.number)
                                    .font(.title3)
                                    .fontWeight(.bold)
                            }
                            .frame(width: 50)

                            VStack(alignment: .leading, spacing: 4) {
                                Text(unit.residentName)
                                    .font(.headline)
                                Text("Tel: \(unit.phone) • Vagas: \(unit.parkingSpaces)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Text(unit.status.rawValue)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(statusColor(unit.status).opacity(0.15))
                                .foregroundColor(statusColor(unit.status))
                                .cornerRadius(8)
                        }
                        .padding(.vertical, 4)
                    }
                }
                .listStyle(.insetGrouped)
            }
            .searchable(text: $searchText, prompt: "Buscar por número, bloco ou morador")
            .navigationTitle("Unidades e Moradores")
            .toolbar {
                Button {
                    showingAddUnitSheet = true
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .font(.title3)
                }
            }
            .sheet(isPresented: $showingAddUnitSheet) {
                AddUnitSheetView()
            }
        }
    }

    private func statusColor(_ status: UnitStatus) -> Color {
        switch status {
        case .compliant: return .green
        case .delinquent: return .red
        case .inAgreement: return .orange
        }
    }
}

// MARK: - FilterChip Helper
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .fontWeight(isSelected ? .bold : .regular)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color(.secondarySystemBackground))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(18)
        }
    }
}

// MARK: - Add Unit Sheet
struct AddUnitSheetView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState

    @State private var number = ""
    @State private var block = "A"
    @State private var residentName = ""
    @State private var phone = ""
    @State private var parkingSpaces = 1
    @State private var status: UnitStatus = .compliant

    var body: some View {
        NavigationStack {
            Form {
                Section("Dados da Unidade") {
                    TextField("Número do Ap/Casa", text: $number)
                    Picker("Bloco", selection: $block) {
                        Text("Bloco A").tag("A")
                        Text("Bloco B").tag("B")
                        Text("Bloco C").tag("C")
                    }
                    Stepper("Vagas de Garagem: \(parkingSpaces)", value: $parkingSpaces, in: 0...5)
                }

                Section("Morador / Proprietário") {
                    TextField("Nome Completo", text: $residentName)
                    TextField("Telefone de Contato", text: $phone)
                    Picker("Situação", selection: $status) {
                        ForEach(UnitStatus.allCases, id: \.self) { st in
                            Text(st.rawValue).tag(st)
                        }
                    }
                }
            }
            .navigationTitle("Nova Unidade")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salvar") {
                        if !number.isEmpty && !residentName.isEmpty {
                            let newUnit = CondUnit(
                                number: number,
                                block: block,
                                residentName: residentName,
                                phone: phone.isEmpty ? "(00) 00000-0000" : phone,
                                status: status,
                                parkingSpaces: parkingSpaces
                            )
                            appState.units.append(newUnit)
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
