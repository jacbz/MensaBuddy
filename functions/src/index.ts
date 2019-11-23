const admin = require("firebase-admin");
const functions = require("firebase-functions");
admin.initializeApp(functions.config().firebase);
let db = admin.firestore();

function stochasticMatching(graph, matchingResult) {
  if (graph.persons.length === 0) {
    return false;
  }

  const person1 = graph.persons[getRandomIntBetween(0, graph.persons.length)];
  const possibleEdges = getEdgesConnectedTo(person1, graph);

  if (possibleEdges.length === 0) {
    return false;
  }

  const connectingEdge =
    possibleEdges[getRandomIntBetween(0, possibleEdges.length)];
  const person2 = selectPartner(connectingEdge, person1);

  const reducedGraph = copyGraph(graph);
  removeEdgesFor(person1, reducedGraph);
  removeEdgesFor(person2, reducedGraph);
  removePerson(person1, reducedGraph);
  removePerson(person2, reducedGraph);

  stochasticMatching(reducedGraph, matchingResult);

  matchingResult.push(connectingEdge);

  return true;
}

function removePerson(person, graph) {
  graph.persons = graph.persons.filter(p => p !== person);
}

function removeEdgesFor(person, graph) {
  const newEdges = [];
  graph.edges.forEach((edge, index) => {
    if (!areAssociated(person, edge)) {
      newEdges.push(edge);
    }
  });
  graph.edges = newEdges;
}

//do not need to deep-copy persons and edges because they shall be immutable
function copyGraph(graph) {
  return {
    persons: [...graph.persons],
    edges: [...graph.edges]
  };
}

function selectPartner(edge, person) {
  if (person === edge.person1) {
    return edge.person2;
  } else if (person === edge.person2) {
    return edge.person1;
  } else {
    return null;
  }
}

function getEdgesConnectedTo(person, graph) {
  const edgesForPerson = [];
  graph.edges.forEach((edge, index) => {
    if (areAssociated(person, edge)) {
      edgesForPerson.push(edge);
    }
  });
  return edgesForPerson;
}

function areAssociated(person, edge) {
  return edge.person1 === person || edge.person2 === person;
}

//a inclusive, b inclusive
function getRandomIntBetween(a, b) {
  return Math.floor(Math.random() * (b - a) + a);
}

function haveOverlap(person1, person2) {
  if (
    person1.interval_end_hour < person2.interval_start_hour ||
    person2.interval_end_hour < person1.interval_start_hour
  ) {
    return false;
  } else if (
    person1.interval_end_hour === person2.interval_start_hour &&
    person1.interval_end_minute <= person2.interval_start_minute
  ) {
    return false;
  } else if (
    person2.interval_end_hour === person1.interval_start_hour &&
    person2.interval_end_minute <= person1.interval_start_minute
  ) {
    return true;
  } else {
    return true;
  }
}

function createGraph(persons) {
  const edges = [];

  for (let i = 0; i < persons.length; i++) {
    for (let j = i + 1; j < persons.length; j++) {
      if (haveOverlap(persons[i], persons[j])) {
        const weight = calculateScore(persons[i], persons[j]);
        const edge = { person1: persons[i], person2: persons[j], weight: weight };
        edges.push(edge);
      }
    }
  }

  return { persons: persons, edges: edges };
}

function getAnswersFor(uid) {
  return {
    0: 1,
    1: 2,
    2: 1,
    3: 0,
    4: 2,
    5: 0,
    6: 0,
    7: 1,
    8: 0,
    9: 3
  }
}

function calculateScore(person1, person2) {
  // const minimalAgreement = 3; //how many questions have to be equally answered at least

  const answers1 = getAnswersFor(person1.uid);
  const answers2 = getAnswersFor(person2.uid);
  const keys1 = Object.keys(answers1);
  const keys2 = Object.keys(answers2);
  const keyIntersection = keys1.filter(key => keys2.includes(key));
  let score = 0;

  for (const key in keyIntersection) {
    if (answers1[key] === answers2[key]) {
      score = score + 1;
    }
  }

  //if(score < minimalAgreement) {
  //	score = ;
  //}

  return score;
}

function calculateTotalScore(matchingResult, graph) {
  const penalty = 100; //penalty for unmatched persons. To be revised!

  const cumulatedScore = matchingResult.reduce(
    (sum, currentEdge) => sum + currentEdge.weight,
    0
  );
  const totalScore =
    cumulatedScore -
    (graph.persons.length - 2 * matchingResult.length) * penalty;

  return totalScore;
}

function stochasticOptimalMatching(graph, maxRuns = 100) {
  let bestScore = -999999999;
  let bestMatching = [];

  for (let i = 0; i < maxRuns; i++) {
    const matchingResult = [];
    stochasticMatching(graph, matchingResult);
    const score = calculateTotalScore(matchingResult, graph);

    if (score > bestScore) {
      bestScore = score;
      bestMatching = matchingResult;
    }
  }

  return bestMatching;
}

function squeezeMatchingIntoOutputDataStructure(bestMatching) {
  const matchingResult = []; //this variable has a different structure than all the other matchingResult variables in this code!!
  bestMatching.forEach((edge, index) => {
    const matching = {
      uids: [edge.person1.uid, edge.person2.uid],
      update_time: Date.now()
    };

    const overlap = getOverlap(edge.person1, edge.person2);
    for (const prop in overlap) {
      matching[prop] = overlap[prop];
    }

    matchingResult.push(matching);
  });

  return matchingResult;
}

function getOverlap(person1, person2) {
  let overlap_start_hour = 0;
  let overlap_start_minute = 0;
  let overlap_end_hour = 0;
  let overlap_end_minute = 0;

  if (person1.interval_start_hour > person2.interval_start_hour) {
    overlap_start_hour = person1.interval_start_hour;
    overlap_start_minute = person1.interval_start_minute;
  } else if (person1.interval_start_hour < person2.interval_start_hour) {
    overlap_start_hour = person2.interval_start_hour;
    overlap_start_minute = person2.interval_start_minute;
  } else {
    overlap_start_hour = person1.interval_start_hour;
    overlap_start_minute = Math.floor(
      Math.max(person1.interval_start_minute, person2.interval_start_minute)
    );
  }

  if (person1.interval_end_hour > person2.interval_end_hour) {
    overlap_end_hour = person2.interval_end_hour;
    overlap_end_minute = person2.interval_end_minute;
  } else if (person1.interval_end_hour < person2.interval_end_hour) {
    overlap_end_hour = person1.interval_end_hour;
    overlap_end_minute = person1.interval_end_minute;
  } else {
    overlap_end_hour = person1.interval_end_hour;
    overlap_end_minute = Math.floor(
      Math.min(person1.interval_end_minute, person2.interval_end_minute)
    );
  }

  return {
    interval_start_hour: overlap_start_hour,
    interval_start_minute: overlap_start_minute,
    interval_end_hour: overlap_end_hour,
    interval_end_minute: overlap_end_minute
  };
}

exports.doMatching = functions.firestore
  .document("/matching/{documentId}")
  .onWrite(async (change) => {
    const matchingSnapshot = await db.collection("matching").get();
    const allMatchings = matchingSnapshot.docs.map(doc => doc.data());
                
    const graph = createGraph(allMatchings);
    const bestMatching = stochasticOptimalMatching(graph);
    const matchResults = squeezeMatchingIntoOutputDataStructure(bestMatching);

    const batch = db.batch();
    const matchingResultsRef = db.collection('matchResults');
    matchingResultsRef
      .get()
      .then((snapshot2) => {
        // When there are no documents left, we are done
        if (snapshot2.size > 0) {
          // Delete documents in a batch
          snapshot2.docs.forEach((doc) => {
            batch.delete(doc.ref);
          });          
        } 

        // write new data
        matchResults.forEach((value, index) => {          
          const newMatchResultRef = matchingResultsRef.doc();
          batch.set(newMatchResultRef, value);
        });

        return batch.commit();
      })  
  });
