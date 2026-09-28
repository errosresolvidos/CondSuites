import SwiftUI

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        Group {
            if appState.isAuthenticated && appState.currentUser != nil {
                MainTabView()
            } else {
                LoginView()
            }
        }
    }
}

// MARK: - Main Tab Bar View for iPhone
struct MainTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Início", systemName: "house.fill")
                }

            UnitsView()
                .tabItem {
                    Label("Unidades", systemName: "building.2.fill")
                }

            OccurrencesView()
                .tabItem {
                    Label("Ocorrências", systemName: "exclamationmark.triangle.fill")
                }

            FinanceView()
                .tabItem {
                    Label("Financeiro", systemName: "dollarsign.circle.fill")
                }

            MoreMenuView()
                .tabItem {
                    Label("Mais", systemName: "ellipsis.circle.fill")
                }
        }
    }
}

// MARK: - More Menu Navigation View
struct MoreMenuView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Gestão & Operação") {
                    NavigationLink {
                        AgreementsView()
                    } label: {
                        Label("Acordos de Cobrança", systemName: "handshake.fill")
                    }

                    NavigationLink {
                        LawsuitsView()
                    } label: {
                        Label("Ações Judiciais (Ajuizados)", systemName: "gavel")
                    }

                    NavigationLink {
                        ElevatorsView()
                    } label: {
                        Label("Gestão de Elevadores", systemName: "elevator")
                    }

                    NavigationLink {
                        ContractsView()
                    } label: {
                        Label("Contratos de Terceiros", systemName: "doc.text.fill")
                    }

                    NavigationLink {
                        OvertimeView()
                    } label: {
                        Label("Lançar Horas Extras", systemName: "clock.badge.plus")
                    }
                }

                Section("Análises & Relatórios") {
                    NavigationLink {
                        ReportsView()
                    } label: {
                        Label("Relatórios Gerenciais", systemName: "chart.bar.doc.horizontal.fill")
                    }
                }

                Section("Ajustes") {
                    NavigationLink {
                        SettingsView()
                    } label: {
                        Label("Configurações", systemName: "gearshape.fill")
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Mais Opções")
        }
    }
}
