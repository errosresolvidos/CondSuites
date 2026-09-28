import SwiftUI

struct ElevatorsView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        NavigationStack {
            List {
                ForEach(appState.elevators) { elevator in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Image(systemName: "elevator")
                                .font(.title2)
                                .foregroundColor(.blue)

                            VStack(alignment: .leading, spacing: 2) {
                                Text("\(elevator.name) - \(elevator.block)")
                                    .font(.headline)
                                Text("Empresa Contratada: \(elevator.company)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Text(elevator.status.rawValue)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(statusColor(elevator.status).opacity(0.15))
                                .foregroundColor(statusColor(elevator.status))
                                .cornerRadius(8)
                        }

                        Divider()

                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Última Manutenção")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(elevator.lastMaintenance.formatted(date: .numeric, time: .omitted))
                                    .font(.caption)
                                    .fontWeight(.medium)
                            }

                            Spacer()

                            VStack(alignment: .trailing, spacing: 2) {
                                Text("Próxima Revisão")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(elevator.nextMaintenance.formatted(date: .numeric, time: .omitted))
                                    .font(.caption)
                                    .fontWeight(.medium)
                                    .foregroundColor(.blue)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Gestão de Elevadores")
        }
    }

    private func statusColor(_ status: ElevatorStatus) -> Color {
        switch status {
        case .operational: return .green
        case .maintenance: return .orange
        case .stopped: return .red
        }
    }
}
