// Function to request data from the server to send to the client
function requestDataFromServer(url, updateFunction)
{
  fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'application/json'
    }
  }).then(response => {if (!response.ok){throw new Error('Network response not ok');}
    return response.json();})
    .then(data => {
      updateFunction(data);
    })
      .catch(error => console.log(error));
}

//Adding the requested data to the client in the form of a table
function updateUI(data){
  const dataTableBody = document.querySelector('#data-table tbody');

  dataTableBody.innerHTML='';

  for(let i=0;i < data.length;i++){
    const entry = data[i];
    const tableRow = document.createElement('tr');

    const fileCell = document.createElement('td');
    fileCell.textContent = entry.file;

    const spamProbabilityCell = document.createElement('td');
    spamProbabilityCell.textContent = entry.spamProbability;

    const actualClassCell = document.createElement('td');
    actualClassCell.textContent = entry.actualClass;

    tableRow.appendChild(fileCell);
    tableRow.appendChild(spamProbabilityCell);
    tableRow.appendChild(actualClassCell);

    dataTableBody.appendChild(tableRow);
  }
}

//Adding the values for accuracy to the client from the server
function updateAccuracy(accuracy){
  const accuracyElement = document.querySelector('#accuracy');
  accuracyElement.textContent = `Accuracy : ${accuracy}`;
}

//Adding the values for precision to the client from the server
function updatePrecision(precision){
  const precisionElement = document.querySelector('#precision');
  precisionElement.textContent = `Precision : ${precision}`;
}

//Declaring the sources from which the server is fetching the data for the client and calling the functions
function onLoad(){
  let apiUrl = "http://localhost:8080/spamDetector-1.0/api/spam/data"
  let accuracyUrl = "http://localhost:8080/spamDetector-1.0/api/spam/accuracy"
  let precisionUrl = "http://localhost:8080/spamDetector-1.0/api/spam/precision"

  requestDataFromServer(apiUrl, updateUI);

  requestDataFromServer(accuracyUrl, updateAccuracy);

  requestDataFromServer(precisionUrl, updatePrecision);
}

document.addEventListener('DOMContentLoaded', onLoad);


function w3_close() {
  document.getElementById("mySidebar").style.display = "none";
  document.getElementById("myOverlay").style.display = "none";
}
