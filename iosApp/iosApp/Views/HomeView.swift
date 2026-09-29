import SwiftUI

struct HomeView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingNewOccurrenceSheet = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header Banner
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Bem-vindo(a), \(appState.currentUser?.username.capitalized ?? "Usuário")")
                                .font(.title2)
                                .fontWeight(.bold)
                            Text("Condomínio Residencial CondSuites")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "building.2.fill")
                            .font(.system(size: 32))
                            .foregroundColor(.blue)
                    }
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(16)

                    // Metric Summary Cards Grid
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                        MetricCard(title: "Inadimplência", value: "12,5%", icon: "percent", color: .orange)
                        MetricCard(title: "Ocorrências", value: "\(appState.occurrences.filter { $0.status != .resolved }.count) Abertas", icon: "exclamationmark.triangle.fill", color: .red)
                        MetricCard(title: "Elevadores", value: "\(appState.elevators.filter { $0.status == .operational }.count)/\(appState.elevators.count) OK", icon: "arrow.up.and.down.square.fill", color: .green)
                        MetricCard(title: "Horas Extras", value: "5.5h Mês", icon: "clock.fill", color: .purple)
                    }

                    // Quick Action Shortcuts
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Ações Rápidas")
                            .font(.headline)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                QuickActionButton(title: "Nova Ocorrência", icon: "square.and.pencil", color: .blue) {
                                    showingNewOccurrenceSheet = true
                                }
                                QuickActionButton(title: "Novo Acordo", icon: "handshake.fill", color: .green) {}
                                QuickActionButton(title: "Lançar Horas", icon: "clock.badge.checkmark", color: .purple) {}
                                QuickActionButton(title: "Unidades", icon: "house.fill", color: .orange) {}
                            }
                        }
                    }

                    // Recent Occurrences Section
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Text("Ocorrências Recentes")
                                .font(.headline)
                            Spacer()
                            NavigationLink(destination: OccurrencesView()) {
                                Text("Ver Todas")
                                    .font(.subheadline)
                            }
                        }

                        ForEach(appState.occurrences.prefix(3)) { occurrence in
                            HStack(spacing: 14) {
                                Circle()
                                    .fill(occurrence.priority == .high ? Color.red : (occurrence.priority == .medium ? Color.orange : Color.blue))
                                    .frame(width: 10, height: 10)

                                VStack(alignment: .leading, spacing: 4) {
                                    Text(occurrence.title)
                                        .font(.headline)
                                    Text("\(occurrence.unit) • \(occurrence.category)")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Text(occurrence.status.rawValue)
                                    .font(.caption2)
                                    .fontWeight(.bold)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.blue.opacity(0.12))
                                    .foregroundColor(.blue)
                                    .cornerRadius(8)
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: .black.opacity(0.04), radius: 5, x: 0, y: 2)
                        }
                    }

                    // Elevator Status Summary
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Status dos Elevadores")
                            .font(.headline)

                        ForEach(appState.elevators) { elevator in
                            HStack {
                                Image(systemName: "arrow.up.and.down.square.fill")
                                    .foregroundColor(.blue)
                                VStack(alignment: .leading) {
                                    Text("\(elevator.name) - \(elevator.block)")
                                        .font(.subheadline)
                                        .fontWeight(.semibold)
                                    Text("Empresa: \(elevator.company)")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Text(elevator.status.rawValue)
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(elevator.status == .operational ? .green : (elevator.status == .maintenance ? .orange : .red))
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: .black.opacity(0.03), radius: 3, x: 0, y: 1)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Início")
            .sheet(isPresented: $showingNewOccurrenceSheet) {
                RegisterOccurrenceView()
            }
        }
    }
}

// MARK: - Subcomponents
struct MetricCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                    .font(.title3)
                Spacer()
            }
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 6, x: 0, y: 3)
    }
}

struct QuickActionButton: View {
    let title: String
    let icon: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .foregroundColor(color)
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(Color.white)
            .cornerRadius(20)
            .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
        }
    }
}
